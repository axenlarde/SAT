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
		ArrayList<CDR> cdrListByStartingTime = new ArrayList<CDR>();
		ArrayList<CDR> cdrListByEndingTime = new ArrayList<CDR>();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
		
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
			boolean keepZeroDuration = Boolean.parseBoolean(UsefulMethod.getTargetOption("keepzeroduration"));
			
			FileReader fileReader= null;
			BufferedReader tampon  = null;
			for(File file : fileList)
				{
				try
					{
					fileReader = new FileReader(file);
					tampon = new BufferedReader(fileReader);
					String inputLine = new String(); 
					
					Integer startTimeIndex, endTimeIndex, callingNumerIndex, calledNumberIndex, callingNameIndex, calledNameIndex, ccmIDIndex, durationIndex;
					startTimeIndex = null;
					endTimeIndex = null;
					callingNumerIndex = null;
					calledNumberIndex = null;
					callingNameIndex = null;
					calledNameIndex = null;
					ccmIDIndex = null;
					durationIndex = null;
							
					String startTimeString, endTimeString, callingNumerString, calledNumberString, callingNameString, calledNameString, ccmIDString, durationString; 
					startTimeString = UsefulMethod.getTargetOption("starttime");
					endTimeString = UsefulMethod.getTargetOption("endtime");
					callingNumerString = UsefulMethod.getTargetOption("callingnumber");
					calledNumberString = UsefulMethod.getTargetOption("callednumber");
					callingNameString = UsefulMethod.getTargetOption("callingname");
					calledNameString = UsefulMethod.getTargetOption("calledname");
					ccmIDString = UsefulMethod.getTargetOption("ccmid");
					durationString = UsefulMethod.getTargetOption("duration");
					
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
								else if(firstLine[i].equals(ccmIDString))ccmIDIndex = i;
								else if(firstLine[i].equals(durationString))durationIndex = i;
								}
							
							if((startTimeIndex == null) ||
									(endTimeIndex == null) ||
									(callingNumerIndex == null) ||
									(calledNumberIndex == null) ||
									(callingNameIndex == null) ||
									(calledNameIndex == null) ||
									(ccmIDIndex == null) ||
									(durationIndex == null))
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
									values[calledNameIndex],
									values[ccmIDIndex],
									Integer.parseInt(values[durationIndex]));
							
							//According to the keepZeroDuration parameters, we discard the zero duration
							if(cdr.getDuration() == 0)
								{
								if(keepZeroDuration)cdrListByStartingTime.add(cdr);
								}
							else
								{
								cdrListByStartingTime.add(cdr);
								}
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
		
		Variables.getLogger().info("Found "+cdrListByStartingTime.size()+" CDR to process");
		
		/**
		 * 3 : We filter the CDR on the given filter basis
		 */
		Variables.getLogger().debug("We keep only filterd values");
		ArrayList<CDR> tempList = new ArrayList<CDR>();
		
		String filter = "AN";
		Variables.getLogger().debug("Filtering using the following String : "+filter);
		
		for(CDR cdr : cdrListByStartingTime)
			{
			//tempList.add(cdr);
			
			/*
			if(cdr.getCcmID().contains("2"))
				{
				tempList.add(cdr);
				}*/
			
			/*
			if(cdr.getCalledNumber().startsWith(filter) || cdr.getCallingNumber().startsWith(filter))
				{
				if(cdr.getCalledName().startsWith("CNAF-")
						|| cdr.getCallingName().startsWith("CNAF-")
						|| cdr.getCallingName().startsWith("SIP_TRK_SFR")
						|| cdr.getCalledName().startsWith("SIP_TRK_SFR"))
					{
					tempList.add(cdr);
					}
				else
					{
					//Nothing
					}
				}*/
			
			if(cdr.getCalledName().startsWith(filter) || cdr.getCallingName().startsWith(filter))
				{
				tempList.add(cdr);
				}
			/*
			if(cdr.getCalledName().contains("@") || cdr.getCallingName().contains("@"))//Keeps only gateways
				{
				if(cdr.getCalledName().contains("DS1-0@gw2921") || cdr.getCallingName().contains("DS1-0@gw2921"))
					{
					tempList.add(cdr);
					}
				}*/
			}
		
		cdrListByStartingTime = tempList;//To free up memory
		System.gc();//Just to be sure
		Variables.getLogger().info("Filtered !");
		Variables.getLogger().debug(cdrListByStartingTime.size()+" CDR remaining");
		if(cdrListByStartingTime.size() == 0)
			{
			Variables.getLogger().debug("No CDR remaining after filtering : Exit");
			System.exit(0);
			}
		
		/*
		Variables.getLogger().info(cdrList.get(100).getCallingName()+" -> "+cdrList.get(100).getCalledName());
		Variables.getLogger().info(cdrList.get(1000).getCallingName()+" -> "+cdrList.get(1000).getCalledName());
		Variables.getLogger().info(cdrList.get(2000).getCallingName()+" -> "+cdrList.get(2000).getCalledName());
		*/
		/*
		ArrayList<String> deviceList = new ArrayList<String>();
		for(CDR cdr : cdrList)
			{
			if((!deviceList.contains(cdr.getCalledName())) && (cdr.getCalledName().startsWith(filter)))
				{
				deviceList.add(cdr.getCalledName());
				}
			else if((!deviceList.contains(cdr.getCallingName())) && (cdr.getCallingName().startsWith(filter)))
				{
				deviceList.add(cdr.getCallingName());
				}
			}
		Variables.getLogger().info(deviceList.size()+" Filtered device found");
		for(String s : deviceList)Variables.getLogger().info(s);*/
		
		
		/******
		 * 4 : Sort by start time and end time
		 */
		ArrayList<CDR> sortedCDRList = new ArrayList<CDR>();
		
		//We sort the calls by start time ascending
		Variables.getLogger().debug("Sorting the CDR by start time");
		
		int index = 1;
		int lastPercent = 0;
		for(CDR cdr : cdrListByStartingTime)
			{
			float percent = (float)index/(float)cdrListByStartingTime.size()*(float)100;
			if((int)percent>lastPercent)
				{
				lastPercent = (int)percent;
				Variables.getLogger().debug("Sorting calls ascending : "+lastPercent+"%");
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
		Variables.getLogger().debug("Ascending Sorted !");
		
		cdrListByStartingTime = sortedCDRList;//We place the sorted result in the original list
		sortedCDRList = null;//To free up memory
		System.gc();//Just to be sure
		
		sortedCDRList = new ArrayList<CDR>();
		
		//We sort the calls by start time descending
		Variables.getLogger().debug("Sorting the CDR by end time");
		
		index = 1;
		lastPercent = 0;
		for(CDR cdr : cdrListByStartingTime)
			{
			float percent = (float)index/(float)cdrListByStartingTime.size()*(float)100;
			if((int)percent>lastPercent)
				{
				lastPercent = (int)percent;
				Variables.getLogger().debug("Sorting calls descending : "+lastPercent+"%");
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
					if(cdr.getEndTime().getTime() >= sortedCDRList.get(i-1).getEndTime().getTime())break;
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
		Variables.getLogger().debug("Descending Sorted !");
		
		cdrListByEndingTime = sortedCDRList;//We place the sorted result in the original list
		sortedCDRList = null;//To free up memory
		System.gc();//Just to be sure
		
		/******
		 * 5 : Calculation of the max concurrent calls
		 * 
		 * In addition gives the time shift when it happens
		 */
		Variables.getLogger().debug("Looking for the max concurrent calls");
		int maxConcurrentcalls = 0;
		ArrayList<CDR> currentCalls = new ArrayList<CDR>();
		ArrayList<CDR> currentCallsLog = new ArrayList<CDR>();
		Date startTime = new Date();
		Date endTime = new Date();
		
		for(CDR cdr : cdrListByStartingTime)
			{
			currentCalls = removeStaleCalls(currentCalls, cdr);
			currentCalls.add(cdr);
			
			if(currentCalls.size()>maxConcurrentcalls)
				{
				maxConcurrentcalls = currentCalls.size();
				startTime = currentCalls.get(0).getStartTime();
				endTime = currentCalls.get(0).getEndTime();
				for(CDR cc : currentCalls)//Then we find the real end time
					{
					if(cc.getEndTime().compareTo(endTime) > 0)endTime=cc.getEndTime();
					}
				
				currentCallsLog.clear();
				currentCallsLog.addAll(currentCalls);
				}
			}
		
		Variables.getLogger().info("Max concurrent calls found : "+maxConcurrentcalls);
		Variables.getLogger().info("Between "+timeFormat.format(startTime)+" and "+timeFormat.format(endTime)+" , date "+dateFormat.format(startTime));
		
		/*Variables.getLogger().debug("Max concurrent calls detail : ");
		
		for(CDR cdr : currentCallsLog)
			{
			Variables.getLogger().debug(timeFormat.format(cdr.getStartTime())+" to "+timeFormat.format(cdr.getEndTime()));
			}*/
		
		/******
		 * 6 : Calculation of the max concurrent calls the same second
		 * 
		 * Should count both starting calls and ending calls. Indeed both are resource consuming
		 * and should be measured
		 * 
		 * In addition gives the time shift when it happens
		 */
		ArrayList<CDR> callsStarting = new ArrayList<CDR>();
		ArrayList<CDR> callsEnding = new ArrayList<CDR>();
		currentCalls = new ArrayList<CDR>();
		
		/***
		 * First we get the number of calls starting the same second
		 */
		for(CDR cdr : cdrListByStartingTime)
			{
			if(currentCalls.size() == 0)currentCalls.add(cdr);
			else
				{
				if(secondFormat.format(cdr.getStartTime()).equals(secondFormat.format(currentCalls.get(0).getStartTime())))
					{
					currentCalls.add(cdr);
					}
				else
					{
					if(currentCalls.size() > callsStarting.size())
						{
						callsStarting.clear();
						callsStarting.addAll(currentCalls);
						}
					currentCalls.clear();
					currentCalls.add(cdr);
					}
				}
			}
		/***
		 * Second we get the number of calls ending the same second
		 */
		currentCalls = new ArrayList<CDR>();
		for(CDR cdr : cdrListByEndingTime)
			{
			if(currentCalls.size() == 0)currentCalls.add(cdr);
			else
				{
				if(secondFormat.format(cdr.getEndTime()).equals(secondFormat.format(currentCalls.get(0).getEndTime())))
					{
					currentCalls.add(cdr);
					}
				else
					{
					if(currentCalls.size() > callsEnding.size())
						{
						callsEnding.clear();
						callsEnding.addAll(currentCalls);
						}
					currentCalls.clear();
					currentCalls.add(cdr);
					}
				}
			}
		/***
		 * Finally we compare then we complement the highest with its opposite
		 * 
		 * So if the calls Ending the same second is higher, we look for the calls starting the same second
		 * To get a really accurate value and vice versa
		 * 
		 * With this method we count a call starting and ending the same second as 1 call
		 * I decided to count it as 2 call because it consume twice as much resource the same second
		 * So we can get the same call twice in the calls arrayList
		 * -> This is the expected behavior
		 */
		if(callsEnding.size() > callsStarting.size())
			{
			String format = dateFormat.format(callsEnding.get(0).getEndTime())+timeFormat.format(callsEnding.get(0).getEndTime());
			for(CDR cdr : cdrListByStartingTime)//We complement with call starting at the same second
				{
				if((dateFormat.format(cdr.getStartTime())+timeFormat.format(cdr.getStartTime())).equals(format))
					{
					//Means that we are at the same second (Also the same day, the same hour, the same minute)
					callsEnding.add(cdr);
					}
				}
			currentCalls = callsEnding;
			}
		else
			{
			String format = dateFormat.format(callsStarting.get(0).getStartTime())+timeFormat.format(callsStarting.get(0).getStartTime());
			for(CDR cdr : cdrListByEndingTime)//We complement with call ending at the same second
				{
				if((dateFormat.format(cdr.getEndTime())+timeFormat.format(cdr.getEndTime())).equals(format))
					{
					//Means that we are at the same second (Also the same day, the same hour, the same minute)
					callsStarting.add(cdr);
					}
				}
			currentCalls = callsStarting;
			}
		
		Variables.getLogger().debug("Max calls per second found : "+currentCalls.size());
		Variables.getLogger().info("At "+timeFormat.format(currentCalls.get(0).getStartTime())+" , date "+dateFormat.format(currentCalls.get(0).getStartTime()));
		for(CDR cdr : currentCalls)
			{
			Variables.getLogger().debug(timeFormat.format(cdr.getStartTime())+" to "+timeFormat.format(cdr.getEndTime()));
			}
		
		/******
		 * 7 : Calculation of the max call per day
		 * 
		 * Will give the date of the day with the most call
		 */
		currentCalls = new ArrayList<CDR>();
		int maxCalls = 0;
		Date mostCallDay = new Date();
		
		for(CDR cdr : cdrListByStartingTime)
			{
			if(currentCalls.size() == 0)currentCalls.add(cdr);
			else
				{
				if(dateFormat.format(cdr.getStartTime()).equals(dateFormat.format(currentCalls.get(0).getStartTime())))
					{
					currentCalls.add(cdr);
					}
				else
					{
					if(currentCalls.size() > maxCalls)
						{
						maxCalls = currentCalls.size();
						mostCallDay = currentCalls.get(0).getStartTime();
						}
					currentCalls.clear();
					currentCalls.add(cdr);
					}
				}
			}
		
		if((currentCalls.size() == cdrListByStartingTime.size()) && (maxCalls == 0))//Means that there is only one day
			{
			maxCalls = currentCalls.size();
			mostCallDay = currentCalls.get(0).getStartTime();
			}
		
		Variables.getLogger().debug("Max call the same day "+maxCalls+", the "+dateFormat.format(mostCallDay));
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
