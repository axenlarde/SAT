package com.alex.sat.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.alex.sat.cdr.CDR;
import com.alex.sat.utils.UsefulMethod;
import com.alex.sat.utils.Variables;

/**
 * used to start actions
 *
 * @author Alexandre RATEL
 */
public class Action
	{
	
	public Action()
		{
		ArrayList<CDR> cdrList = new ArrayList<CDR>();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss:SSS");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		
		/**
		 * Timezone
		 */
		
		try
			{
			String timeZone = UsefulMethod.getTargetOption("timezone");
			timeFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			}
		catch (Exception e)
			{
			Variables.getLogger().error("ERROR while setting up the timezone");
			e.printStackTrace();
			System.exit(0);
			}
		
		try
			{
			/**
			 * 1 : We get the files to process
			 */
			Variables.setSourceDirectory(Variables.getMainDirectory()+"/"+UsefulMethod.getTargetOption("sourcedirectory")+"/");
			ArrayList<File> fileList = UsefulMethod.getFilesToProcess(Variables.getSourceDirectory());
			
			String splitter = UsefulMethod.getTargetOption("csvsplitter");
			
			FileReader fileReader= null;
			BufferedReader tampon  = null;
			for(File file : fileList)
				{
				try
					{
					fileReader = new FileReader(file);
					tampon = new BufferedReader(fileReader);
					String inputLine = new String(); 
					
					Integer startTimeIndex, endTimeIndex, callingNumerIndex, calledNumberIndex, callingNameIndex, calledNameIndex;
					startTimeIndex = null;
					endTimeIndex = null;
					callingNumerIndex = null;
					calledNumberIndex = null;
					callingNameIndex = null;
					calledNameIndex = null;
							
					String startTimeString, endTimeString, callingNumerString, calledNumberString, callingNameString, calledNameString; 
					startTimeString = UsefulMethod.getTargetOption("starttime");
					endTimeString = UsefulMethod.getTargetOption("endtime");
					callingNumerString = UsefulMethod.getTargetOption("callingnumber");
					calledNumberString = UsefulMethod.getTargetOption("callednumber");
					callingNameString = UsefulMethod.getTargetOption("callingname");
					calledNameString = UsefulMethod.getTargetOption("calledname");
					
					boolean firstline = true;
					int index = 1;
					int lastPercent = 0;
					int lineNumber = UsefulMethod.countLines(file);
					Variables.getLogger().debug("File line count : "+lineNumber);
					
					/**
					 * 2 : We process the files to get the CDR
					 */
					while (((inputLine = tampon.readLine()) != null) && (inputLine.compareTo("") !=0))
			        	{
			        	if(firstline)
			        		{
			        		firstline = false;
			        		String[] firstLine = inputLine.split(splitter);
			        		
							//We get the index of each needed value
							for(int i=0; i<firstLine.length; i++)
								{
								if(firstLine[i].equals(startTimeString))startTimeIndex = i;
								else if(firstLine[i].equals(endTimeString))endTimeIndex = i;
								else if(firstLine[i].equals(callingNumerString))callingNumerIndex = i;
								else if(firstLine[i].equals(calledNumberString))calledNumberIndex = i;
								else if(firstLine[i].equals(callingNameString))callingNameIndex = i;
								else if(firstLine[i].equals(calledNameString))calledNameIndex = i;
								}
							
							if((startTimeIndex == null) ||
									(endTimeIndex == null) ||
									(callingNumerIndex == null) ||
									(calledNumberIndex == null) ||
									(callingNameIndex == null) ||
									(calledNameIndex == null))
								{
								Variables.getLogger().error("ERROR : One or more value was not found : exit");
								System.exit(0);
								}
			        		}
			        	else
			        		{
							//To display the processing percentage
							float percent = (float)index/(float)lineNumber*(float)100;
							if((int)percent>lastPercent)
								{
								lastPercent = (int)percent;
								Variables.getLogger().debug("Processing lines : "+lastPercent+"%");
								}
							//
							
							String[] values = inputLine.split(splitter);
							CDR cdr = new CDR(new Date(Long.parseLong(values[startTimeIndex])*1000),
									new Date(Long.parseLong(values[endTimeIndex])*1000),
									values[callingNumerIndex],
									values[calledNumberIndex],
									values[callingNameIndex],
									values[calledNameIndex]);
							
							if(cdr.getStartTime().getTime() != 0)cdrList.add(cdr);
							index++;
			        		}
			         	}
					}
				catch(IOException exception)
					{
					exception.printStackTrace();
					throw new Exception("ERROR : Failed to read the following file : "+file.getName());
					}
				finally
					{
					try
						{
						tampon.close();
						fileReader.close();
						}
					catch(Exception e)
						{
						e.printStackTrace();
						throw new Exception("ERROR : Failed to read the following file : "+file.getName());
						}
					}
				}
			}
		catch (Exception e)
			{
			Variables.getLogger().error("ERROR while processing the files : "+e.getMessage(),e);
			e.printStackTrace();
			System.exit(0);
			}
		
		Variables.getLogger().info("Found "+cdrList.size()+" CDR to process");
		
		/**
		 * 3 : We filter the CDR on the given filter basis
		 */
		Variables.getLogger().debug("We keep only filterd values");
		ArrayList<CDR> tempList = new ArrayList<CDR>();
		
		for(CDR cdr : cdrList)
			{
			if(cdr.getCalledName().contains("@") || cdr.getCallingName().contains("@"))
				{
				if(cdr.getCalledName().toLowerCase().contains("fr") || cdr.getCallingName().toLowerCase().contains("fr"))
					{
					tempList.add(cdr);
					}
				}
			}
		
		cdrList = tempList;//To free up memory
		System.gc();//Just to be sure
		Variables.getLogger().info("Filtered !");
		Variables.getLogger().debug(cdrList.size()+" CDR remaining");
		
		
		/******
		 * 4 : Sort by start time
		 */
		int maxConcurrentcalls = 0;
		ArrayList<CDR> sortedCDRList = new ArrayList<CDR>();
		
		//We sort the calls by start time ascending
		Variables.getLogger().debug("Sorting the CDR by start time");
		
		int index = 1;
		int lastPercent = 0;
		for(CDR cdr : cdrList)
			{
			float percent = (float)index/(float)cdrList.size()*(float)100;
			if((int)percent>lastPercent)
				{
				lastPercent = (int)percent;
				Variables.getLogger().debug("Sorting calls : "+lastPercent+"%");
				}
			
			if(sortedCDRList.size() == 0)//The first one
				{
				sortedCDRList.add(cdr);
				}
			else
				{
				int i=sortedCDRList.size();
				boolean limitReached = false;
				while(true)
					{
					if(cdr.getStartTime().getTime() >= sortedCDRList.get(i-1).getStartTime().getTime())break;
					if(i<=1)
						{
						limitReached = true;
						break;
						}
					else i--;
					}
				if(limitReached)sortedCDRList.add(0, cdr);
				else sortedCDRList.add(i, cdr);
				}
			index++;
			}
		Variables.getLogger().debug("Sorted !");
		
		cdrList = sortedCDRList;//We place the sorted result in the original list
		sortedCDRList = null;//To free up memory
		System.gc();//Just to be sure
		
		/******
		 * 5 : Calculation of the max concurrent calls
		 * 
		 * In addition gives the time shift when it happens
		 */
		Variables.getLogger().debug("Looking for the max concurrent calls");
		ArrayList<CDR> currentCalls = new ArrayList<CDR>();
		ArrayList<CDR> currentCallsLog = new ArrayList<CDR>();
		Date startTime = new Date();
		Date endTime = new Date();
		
		for(CDR cdr : cdrList)
			{
			currentCalls = removeStaleCalls(currentCalls, cdr);
			currentCalls.add(cdr);
			
			if(currentCalls.size()>maxConcurrentcalls)
				{
				maxConcurrentcalls = currentCalls.size();
				startTime = currentCalls.get(0).getStartTime();
				endTime = currentCalls.get(currentCalls.size()-1).getEndTime();
				currentCallsLog.clear();
				currentCallsLog.addAll(currentCalls);
				}
			}
		
		Variables.getLogger().info("Max concurrent calls found : "+maxConcurrentcalls);
		Variables.getLogger().info("Between "+timeFormat.format(startTime)+" and "+timeFormat.format(endTime)+" , date "+dateFormat.format(startTime));
		Variables.getLogger().debug("Max concurrent calls detail : ");
		for(CDR cdr : currentCallsLog)
			{
			Variables.getLogger().debug(timeFormat.format(cdr.getStartTime())+" to "+timeFormat.format(cdr.getEndTime()));
			}
		}
	
	
	/**
	 * To remove stale call from a given cdr list
	 */
	private ArrayList<CDR> removeStaleCalls(ArrayList<CDR> callList, CDR cdr)
		{
		for(CDR cc : callList)
			{
			if(cdr.getStartTime().getTime() > cc.getEndTime().getTime())
				{
				callList.remove(cc);
				callList = removeStaleCalls(callList, cdr);
				break;
				}
			}
		
		return callList;
		}
	
	/*2020*//*RATEL Alexandre 8)*/
	}