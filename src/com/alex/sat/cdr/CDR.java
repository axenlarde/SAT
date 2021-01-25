package com.alex.sat.cdr;

import java.util.Date;

/**
 * A CUCM CDR (Call Detail record)
 *
 * @author Alexandre RATEL
 */
public class CDR
	{
	/**
	 * Variables
	 */
	private Date startTime, endTime;
	private String callingNumber, calledNumber, callingName, calledName, ccmID;
	private int duration;
	
	public CDR(Date startTime, Date endTime, String callingNumber, String calledNumber, String callingName,
			String calledName, String ccmID, int duration)
		{
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.callingNumber = callingNumber;
		this.calledNumber = calledNumber;
		this.callingName = callingName;
		this.calledName = calledName;
		this.ccmID = ccmID;
		this.duration = duration;
		}

	public Date getStartTime()
		{
		return startTime;
		}

	public Date getEndTime()
		{
		return endTime;
		}

	public String getCallingNumber()
		{
		return callingNumber;
		}

	public String getCalledNumber()
		{
		return calledNumber;
		}

	public String getCallingName()
		{
		return callingName;
		}

	public String getCalledName()
		{
		return calledName;
		}

	public String getCcmID()
		{
		return ccmID;
		}

	public int getDuration()
		{
		return duration;
		}
	
	
	
	/*2020*//*RATEL Alexandre 8)*/
	}
