package com.alex.sat.cdr;

public class CDRTools
	{
	
	public static boolean isConcurrent(CDR cdr1, CDR cdr2)
		{
		if((cdr2.getStartTime().getTime()>=cdr1.getStartTime().getTime()) &&
				(cdr2.getStartTime().getTime()<=cdr1.getEndTime().getTime()))
			{
			return true;
			}
		return false;
		}
	
	/*2020*//*RATEL Alexandre 8)*/
	}
