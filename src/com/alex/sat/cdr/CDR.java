package com.alex.sat.cdr;

import java.util.Date;

/**
 * A CDR (Call Detail record)
 *
 * @author Alexandre RATEL
 */
public class CDR
	{
	/**
	 * Variables
	 */
	private Date startTime, endTime;
	private String callingNumber, calledNumber, callingName, calledName;
	
	public CDR(Date startTime, Date endTime, String callingNumber, String calledNumber, String callingName,
			String calledName)
		{
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.callingNumber = callingNumber;
		this.calledNumber = calledNumber;
		this.callingName = callingName;
		this.calledName = calledName;
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
	
	/*2020*//*RATEL Alexandre 8)*/
	}
