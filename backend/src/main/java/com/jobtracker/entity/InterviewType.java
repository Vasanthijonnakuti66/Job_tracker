package com.jobtracker.entity;

/**
 * Types of interviews a candidate might go through.
 */
public enum InterviewType {
    PHONE,          // Phone screen / intro call
    VIDEO,          // Video call (Zoom, Teams, etc.)
    ONSITE,         // In-person at the company office
    TECHNICAL,      // Coding / system design interview
    HR,             // HR / behavioral / culture-fit round
    OTHER           // Anything else
}
