package com.alex.sat.main;

import org.apache.log4j.Level;

import com.alex.sat.action.Action;
import com.alex.sat.utils.InitLogging;
import com.alex.sat.utils.UsefulMethod;
import com.alex.sat.utils.Variables;


/**
 * SAT main class
 *
 * SAT : SBC Assessment Tool
 * 
 * Used to gives insight about a system calls tendencies such as max simultaneous calls
 *
 * @author Alexandre RATEL
 */
public class Main
	{
	
	public Main()
		{
		//Set the software name
		Variables.setSoftwareName("SAT");
		//Set the software version
		Variables.setSoftwareVersion("1.0");
		
		/****************
		 * Initialization of the logging
		 */
		Variables.setLogFileName(Variables.getSoftwareName()+"_LogFile.txt");
		Variables.setLogger(InitLogging.init(Variables.getLogFileName()));
		Variables.getLogger().info("\r\n");
		Variables.getLogger().info("#### Entering application");
		Variables.getLogger().info("## Welcome to : "+Variables.getSoftwareName()+" version "+Variables.getSoftwareVersion());
		Variables.getLogger().info("## Author : RATEL Alexandre : 2020");
		/*******/
		
		/******
		 * Initialization of the variables
		 */
		new Variables();
		/************/
		
		/**********************
		 * Reading of the configuration files
		 */
		try
			{
			/**
			 * Config file reading
			 */
			Variables.setTabConfig(UsefulMethod.readMainConfigFile(Variables.getConfigFileName()));
			}
		catch(Exception exc)
			{
			UsefulMethod.failedToInit(exc);
			}
		/********************/
		
		/*****************
		 * Setting of the inside variables from what we read in the configuration file
		 */
		try
			{
			UsefulMethod.initInternalVariables();
			}
		catch(Exception exc)
			{
			Variables.getLogger().error(exc.getMessage());
			Variables.getLogger().setLevel(Level.INFO);
			}
		/*********************/
		
		/*******************
		 * Start main class
		 */
		try
			{
			Variables.getLogger().info("Launching Main Class");
			new Action();//main file
			}
		catch (Exception exc)
			{
			UsefulMethod.failedToInit(exc);
			}
		/******************/
		
		//End of the main class
		}
	
	
	

	public static void main(String[] args)
		{
		new Main();
		}
	/*2020*//*RATEL Alexandre 8)*/
	}
