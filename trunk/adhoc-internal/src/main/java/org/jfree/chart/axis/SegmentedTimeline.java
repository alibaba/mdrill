/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -----------------------
 * SegmentedTimeline.java
 * -----------------------
 * (C) Copyright 2003-2008, by Bill Kelemen and Contributors.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 23-May-2003 : Version 1 (BK);
 * 15-Aug-2003 : Implemented Cloneable (DG);
 * 01-Jun-2004 : Modified to compile with JDK 1.2.2 (DG);
 * 30-Sep-2004 : Replaced getTime().getTime() with getTimeInMillis() (DG);
 * 04-Nov-2004 : Reverted change of 30-Sep-2004, won't work with JDK 1.3 (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 14-Nov-2006 : Fix in toTimelineValue(long) to avoid stack overflow (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 * 11-Jul-2007 : Fixed time zone bugs (DG);
 * 06-Jun-2008 : Performance enhancement posted in forum (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * A {@link Timeline} that implements a "segmented" timeline with included,
 * excluded and exception segments.
 * <P>
 * A Timeline will present a series of values to be used for an axis. Each
 * Timeline must provide transformation methods between domain values and
 * timeline values.
 * <P>
 * A timeline can be used as parameter to a
 * {@link org.jfree.chart.axis.DateAxis} to define the values that this axis
 * supports. This class implements a timeline formed by segments of equal
 * length (ex. days, hours, minutes) where some segments can be included in the
 * timeline and others excluded. Therefore timelines like "working days" or
 * "working hours" can be created where non-working days or non-working hours
 * respectively can be removed from the timeline, and therefore from the axis.
 * This creates a smooth plot with equal separation between all included
 * segments.
 * <P>
 * Because Timelines were created mainly for Date related axis, values are
 * represented as longs instead of doubles. In this case, the domain value is
 * just the number of milliseconds since January 1, 1970, 00:00:00 GMT as
 * defined by the getTime() method of {@link java.util.Date}.
 * <P>
 * In this class, a segment is defined as a unit of time of fixed length.
 * Examples of segments are: days, hours, minutes, etc. The size of a segment
 * is defined as the number of milliseconds in the segment. Some useful segment
 * sizes are defined as constants in this class: DAY_SEGMENT_SIZE,
 * HOUR_SEGMENT_SIZE, FIFTEEN_MINUTE_SEGMENT_SIZE and MINUTE_SEGMENT_SIZE.
 * <P>
 * Segments are group together to form a Segment Group. Each Segment Group will
 * contain a number of Segments included and a number of Segments excluded. This
 * Segment Group structure will repeat for the whole timeline.
 * <P>
 * For example, a working days SegmentedTimeline would be formed by a group of
 * 7 daily segments, where there are 5 included (Monday through Friday) and 2
 * excluded (Saturday and Sunday) segments.
 * <P>
 * Following is a diagram that explains the major attributes that define a
 * segment.  Each box is one segment and must be of fixed length (ms, second,
 * hour, day, etc).
 * <p>
 * <pre>
 * start time
 *   |
 *   v
 *   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 ...
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+...
 * |  |  |  |  |  |EE|EE|  |  |  |  |  |EE|EE|  |  |  |  |  |EE|EE|
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+...
 *  \____________/ \___/            \_/
 *        \/         |               |
 *     included   excluded        segment
 *     segments   segments         size
 *  \_________  _______/
 *            \/
 *       segment group
 * </pre>
 * Legend:<br>
 * &lt;space&gt; = Included segment<br>
 * EE      = Excluded segments in the base timeline<br>
 * <p>
 * In the example, the following segment attributes are presented:
 * <ul>
 * <li>segment size: the size of each segment in ms.
 * <li>start time: the start of the first segment of the first segment group to
 *     consider.
 * <li>included segments: the number of segments to include in the group.
 * <li>excluded segments: the number of segments to exclude in the group.
 * </ul>
 * <p>
 * Exception Segments are allowed. These exception segments are defined as
 * segments that would have been in the included segments of the Segment Group,
 * but should be excluded for special reasons. In the previous working days
 * SegmentedTimeline example, holidays would be considered exceptions.
 * <P>
 * Additionally the <code>startTime</code>, or start of the first Segment of
 * the smallest segment group needs to be defined. This startTime could be
 * relative to January 1, 1970, 00:00:00 GMT or any other date. This creates a
 * point of reference to start counting Segment Groups. For example, for the
 * working days SegmentedTimeline, the <code>startTime</code> could be
 * 00:00:00 GMT of the first Monday after January 1, 1970. In this class, the
 * constant FIRST_MONDAY_AFTER_1900 refers to a reference point of the first
 * Monday of the last century.
 * <p>
 * A SegmentedTimeline can include a baseTimeline. This combination of
 * timelines allows the creation of more complex timelines. For example, in
 * order to implement a SegmentedTimeline for an intraday stock trading
 * application, where the trading period is defined as 9:00 AM through 4:00 PM
 * Monday through Friday, two SegmentedTimelines are used. The first one (the
 * baseTimeline) would be a working day SegmentedTimeline (daily timeline
 * Monday through Friday). On top of this baseTimeline, a second one is defined
 * that maps the 9:00 AM to 4:00 PM period. Because the baseTimeline defines a
 * timeline of Monday through Friday, the resulting (combined) timeline will
 * expose the period 9:00 AM through 4:00 PM only on Monday through Friday,
 * and will remove all other intermediate intervals.
 * <P>
 * Two factory methods newMondayThroughFridayTimeline() and
 * newFifteenMinuteTimeline() are provided as examples to create special
 * SegmentedTimelines.
 *
 * @see org.jfree.chart.axis.DateAxis
 */
public class SegmentedTimeline implements Timeline, Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1093779862539903110L;

    ////////////////////////////////////////////////////////////////////////////
    // predetermined segments sizes
    ////////////////////////////////////////////////////////////////////////////

    /** Defines a day segment size in ms. */
    public static final long DAY_SEGMENT_SIZE = 24 * 60 * 60 * 1000;

    /** Defines a one hour segment size in ms. */
    public static final long HOUR_SEGMENT_SIZE = 60 * 60 * 1000;

    /** Defines a 15-minute segment size in ms. */
    public static final long FIFTEEN_MINUTE_SEGMENT_SIZE = 15 * 60 * 1000;

    /** Defines a one-minute segment size in ms. */
    public static final long MINUTE_SEGMENT_SIZE = 60 * 1000;

    ////////////////////////////////////////////////////////////////////////////
    // other constants
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Utility constant that defines the startTime as the first monday after
     * 1/1/1970.  This should be used when creating a SegmentedTimeline for
     * Monday through Friday. See static block below for calculation of this
     * constant.
     *
     * @deprecated As of 1.0.7.  This field doesn't take into account changes
     *         to the default time zone.
     */
    public static long FIRST_MONDAY_AFTER_1900;

    /**
     * Utility TimeZone object that has no DST and an offset equal to the
     * default TimeZone. This allows easy arithmetic between days as each one
     * will have equal size.
     *
     * @deprecated As of 1.0.7.  This field is initialised based on the
     *         default time zone, and doesn't take into account subsequent
     *         changes to the default.
     */
    public static TimeZone NO_DST_TIME_ZONE;

    /**
     * This is the default time zone where the application is running. See
     * getTime() below where we make use of certain transformations between
     * times in the default time zone and the no-dst time zone used for our
     * calculations.
     *
     * @deprecated As of 1.0.7.  When the default time zone is required,
     *         just call <code>TimeZone.getDefault()</code>.
     */
    public static TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    /**
     * This will be a utility calendar that has no DST but is shifted relative
     * to the default time zone's offset.
     */
    private Calendar workingCalendarNoDST;

    /**
     * This will be a utility calendar that used the default time zone.
     */
    private Calendar workingCalendar = Calendar.getInstance();

    ////////////////////////////////////////////////////////////////////////////
    // private attributes
    ////////////////////////////////////////////////////////////////////////////

    /** Segment size in ms. */
    private long segmentSize;

    /** Number of consecutive segments to include in a segment group. */
    private int segmentsIncluded;

    /** Number of consecutive segments to exclude in a segment group. */
    private int segmentsExcluded;

    /** Number of segments in a group (segmentsIncluded + segmentsExcluded). */
    private int groupSegmentCount;

    /**
     * Start of time reference from time zero (1/1/1970).
     * This is the start of segment #0.
     */
    private long startTime;

    /** Consecutive ms in segmentsIncluded (segmentsIncluded * segmentSize). */
    private long segmentsIncludedSize;

    /** Consecutive ms in segmentsExcluded (segmentsExcluded * segmentSize). */
    private long segmentsExcludedSize;

    /** ms in a segment group (segmentsIncludedSize + segmentsExcludedSize). */
    private long segmentsGroupSize;

    /**
     * List of exception segments (exceptions segments that would otherwise be
     * included based on the periodic (included, excluded) grouping).
     */
    private List exceptionSegments = new ArrayList();

    /**
     * This base timeline is used to specify exceptions at a higher level. For
     * example, if we are a intraday timeline and want to exclude holidays,
     * instead of having to exclude all intraday segments for the holiday,
     * segments from this base timeline can be excluded. This baseTimeline is
     * always optional and is only a convenience method.
     * <p>
     * Additionally, all excluded segments from this baseTimeline will be
     * considered exceptions at this level.
     */
    private SegmentedTimeline baseTimeline;

    /** A flag that controls whether or not to adjust for daylight saving. */
    private boolean adjustForDaylightSaving = false;

    ////////////////////////////////////////////////////////////////////////////
    // static block
    ////////////////////////////////////////////////////////////////////////////

    static {
        // make a time zone with no DST for our Calendar calculations
        int offset = TimeZone.getDefault().getRawOffset();
        NO_DST_TIME_ZONE = new SimpleTimeZone(offset, "UTC-" + offset);

        // calculate midnight of first monday after 1/1/1900 relative to
        // current locale
        Calendar cal = new GregorianCalendar(NO_DST_TIME_ZONE);
        cal.set(1900, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, 1);
        }
        // FIRST_MONDAY_AFTER_1900 = cal.getTime().getTime();
        // preceding code won't work with JDK 1.3
        FIRST_MONDAY_AFTER_1900 = cal.getTime().getTime();
    }

    ////////////////////////////////////////////////////////////////////////////
    // constructors and factory methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a new segmented timeline, optionaly using another segmented
     * timeline as its base. This chaining of SegmentedTimelines allows further
     * segmentation into smaller timelines.
     *
     * If a base
     *
     * @param segmentSize the size of a segment in ms. This time unit will be
     *        used to compute the included and excluded segments of the
     *        timeline.
     * @param segmentsIncluded Number of consecutive segments to include.
     * @param segmentsExcluded Number of consecutive segments to exclude.
     */
    public SegmentedTimeline(long segmentSize,
                             int segmentsIncluded,
                             int segmentsExcluded) {

        this.segmentSize = segmentSize;
        this.segmentsIncluded = segmentsIncluded;
        this.segmentsExcluded = segmentsExcluded;

        this.groupSegmentCount = this.segmentsIncluded + this.segmentsExcluded;
        this.segmentsIncludedSize = this.segmentsIncluded * this.segmentSize;
        this.segmentsExcludedSize = this.segmentsExcluded * this.segmentSize;
        this.segmentsGroupSize = this.segmentsIncludedSize
                                 + this.segmentsExcludedSize;
        int offset = TimeZone.getDefault().getRawOffset();
        TimeZone z = new SimpleTimeZone(offset, "UTC-" + offset);
        this.workingCalendarNoDST = new GregorianCalendar(z,
                Locale.getDefault());
    }

    /**
     * Returns the milliseconds for midnight of the first Monday after
     * 1-Jan-1900, ignoring daylight savings.
     *
     * @return The milliseconds.
     *
     * @since 1.0.7
     */
    public static long firstMondayAfter1900() {
        int offset = TimeZone.getDefault().getRawOffset();
        TimeZone z = new SimpleTimeZone(offset, "UTC-" + offset);

        // calculate midnight of first monday after 1/1/1900 relative to
        // current locale
        Calendar cal = new GregorianCalendar(z);
        cal.set(1900, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, 1);
        }
        //return cal.getTimeInMillis();
        // preceding code won't work with JDK 1.3
        return cal.getTime().getTime();
    }

    /**
     * Factory method to create a Monday through Friday SegmentedTimeline.
     * <P>
     * The <code>startTime</code> of the resulting timeline will be midnight
     * of the first Monday after 1/1/1900.
     *
     * @return A fully initialized SegmentedTimeline.
     */
    public static SegmentedTimeline newMondayThroughFridayTimeline() {
        SegmentedTimeline timeline
            = new SegmentedTimeline(DAY_SEGMENT_SIZE, 5, 2);
        timeline.setStartTime(firstMondayAfter1900());
        return timeline;
    }

    /**
     * Factory method to create a 15-min, 9:00 AM thought 4:00 PM, Monday
     * through Friday SegmentedTimeline.
     * <P>
     * This timeline uses a segmentSize of FIFTEEN_MIN_SEGMENT_SIZE. The
     * segment group is defined as 28 included segments (9:00 AM through
     * 4:00 PM) and 68 excluded segments (4:00 PM through 9:00 AM the next day).
     * <P>
     * In order to exclude Saturdays and Sundays it uses a baseTimeline that
     * only includes Monday through Friday days.
     * <P>
     * The <code>startTime</code> of the resulting timeline will be 9:00 AM
     * after the startTime of the baseTimeline. This will correspond to 9:00 AM
     * of the first Monday after 1/1/1900.
     *
     * @return A fully initialized SegmentedTimeline.
     */
    public static SegmentedTimeline newFifteenMinuteTimeline() {
        SegmentedTimeline timeline = new SegmentedTimeline(
                FIFTEEN_MINUTE_SEGMENT_SIZE, 28, 68);
        timeline.setStartTime(firstMondayAfter1900() + 36
                * timeline.getSegmentSize());
        timeline.setBaseTimeline(newMondayThroughFridayTimeline());
        return timeline;
    }

    /**
     * Returns the flag that controls whether or not the daylight saving
     * adjustment is applied.
     *
     * @return A boolean.
     */
    public boolean getAdjustForDaylightSaving() {
        return this.adjustForDaylightSaving;
    }

    /**
     * Sets the flag that controls whether or not the daylight saving adjustment
     * is applied.
     *
     * @param adjust  the flag.
     */
    public void setAdjustForDaylightSaving(boolean adjust) {
        this.adjustForDaylightSaving = adjust;
    }

    ////////////////////////////////////////////////////////////////////////////
    // operations
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the start time for the timeline. This is the beginning of segment
     * zero.
     *
     * @param millisecond  the start time (encoded as in java.util.Date).
     */
    public void setStartTime(long millisecond) {
        this.startTime = millisecond;
    }

    /**
     * Returns the start time for the timeline. This is the beginning of
     * segment zero.
     *
     * @return The start time.
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Returns the number of segments excluded per segment group.
     *
     * @return The number of segments excluded.
     */
    public int getSegmentsExcluded() {
        return this.segmentsExcluded;
    }

    /**
     * Returns the size in milliseconds of the segments excluded per segment
     * group.
     *
     * @return The size in milliseconds.
     */
    public long getSegmentsExcludedSize() {
        return this.segmentsExcludedSize;
    }

    /**
     * Returns the number of segments in a segment group. This will be equal to
     * segments included plus segments excluded.
     *
     * @return The number of segments.
     */
    public int getGroupSegmentCount() {
        return this.groupSegmentCount;
    }

    /**
     * Returns the size in milliseconds of a segment group. This will be equal
     * to size of the segments included plus the size of the segments excluded.
     *
     * @return The segment group size in milliseconds.
     */
    public long getSegmentsGroupSize() {
        return this.segmentsGroupSize;
    }

    /**
     * Returns the number of segments included per segment group.
     *
     * @return The number of segments.
     */
    public int getSegmentsIncluded() {
        return this.segmentsIncluded;
    }

    /**
     * Returns the size in ms of the segments included per segment group.
     *
     * @return The segment size in milliseconds.
     */
    public long getSegmentsIncludedSize() {
        return this.segmentsIncludedSize;
    }

    /**
     * Returns the size of one segment in ms.
     *
     * @return The segment size in milliseconds.
     */
    public long getSegmentSize() {
        return this.segmentSize;
    }

    /**
     * Returns a list of all the exception segments. This list is not
     * modifiable.
     *
     * @return The exception segments.
     */
    public List getExceptionSegments() {
        return Collections.unmodifiableList(this.exceptionSegments);
    }

    /**
     * Sets the exception segments list.
     *
     * @param exceptionSegments  the exception segments.
     */
    public void setExceptionSegments(List exceptionSegments) {
        this.exceptionSegments = exceptionSegments;
    }

    /**
     * Returns our baseTimeline, or <code>null</code> if none.
     *
     * @return The base timeline.
     */
    public SegmentedTimeline getBaseTimeline() {
        return this.baseTimeline;
    }

    /**
     * Sets the base timeline.
     *
     * @param baseTimeline  the timeline.
     */
    public void setBaseTimeline(SegmentedTimeline baseTimeline) {

        // verify that baseTimeline is compatible with us
        if (baseTimeline != null) {
            if (baseTimeline.getSegmentSize() < this.segmentSize) {
                throw new IllegalArgumentException(
                        "baseTimeline.getSegmentSize() "
                        + "is smaller than segmentSize");
            }
            else if (baseTimeline.getStartTime() > this.startTime) {
                throw new IllegalArgumentException(
                        "baseTimeline.getStartTime() is after startTime");
            }
            else if ((baseTimeline.getSegmentSize() % this.segmentSize) != 0) {
                throw new IllegalArgumentException(
                        "baseTimeline.getSegmentSize() is not multiple of "
                        + "segmentSize");
            }
            else if (((this.startTime
                    - baseTimeline.getStartTime()) % this.segmentSize) != 0) {
                throw new IllegalArgumentException(
                        "baseTimeline is not aligned");
            }
        }

        this.baseTimeline = baseTimeline;
    }

    /**
     * Translates a value relative to the domain value (all Dates) into a value
     * relative to the segmented timeline. The values relative to the segmented
     * timeline are all consecutives starting at zero at the startTime.
     *
     * @param millisecond  the millisecond (as encoded by java.util.Date).
     *
     * @return The timeline value.
     */
    public long toTimelineValue(long millisecond) {

        long result;
        long rawMilliseconds = millisecond - this.startTime;
        long groupMilliseconds = rawMilliseconds % this.segmentsGroupSize;
        long groupIndex = rawMilliseconds / this.segmentsGroupSize;

        if (groupMilliseconds >= this.segmentsIncludedSize) {
            result = toTimelineValue(this.startTime + this.segmentsGroupSize
                    * (groupIndex + 1));
        }
        else {
            Segment segment = getSegment(millisecond);
            if (segment.inExceptionSegments()) {
                int p;
                while ((p = binarySearchExceptionSegments(segment)) >= 0) {
                    segment = getSegment(millisecond = ((Segment)
                            this.exceptionSegments.get(p)).getSegmentEnd() + 1);
                }
                result = toTimelineValue(millisecond);
            }
            else {
                long shiftedSegmentedValue = millisecond - this.startTime;
                long x = shiftedSegmentedValue % this.segmentsGroupSize;
                long y = shiftedSegmentedValue / this.segmentsGroupSize;

                long wholeExceptionsBeforeDomainValue =
                    getExceptionSegmentCount(this.startTime, millisecond - 1);

//                long partialTimeInException = 0;
//                Segment ss = getSegment(millisecond);
//                if (ss.inExceptionSegments()) {
//                    partialTimeInException = millisecond
                //     - ss.getSegmentStart();
//                }

                if (x < this.segmentsIncludedSize) {
                    result = this.segmentsIncludedSize * y
                             + x - wholeExceptionsBeforeDomainValue
                             * this.segmentSize;
                             // - partialTimeInException;
                }
                else {
                    result = this.segmentsIncludedSize * (y + 1)
                             - wholeExceptionsBeforeDomainValue
                             * this.segmentSize;
                             // - partialTimeInException;
                }
            }
        }

        return result;
    }

    /**
     * Translates a date into a value relative to the segmented timeline. The
     * values relative to the segmented timeline are all consecutives starting
     * at zero at the startTime.
     *
     * @param date  date relative to the domain.
     *
     * @return The timeline value (in milliseconds).
     */
    public long toTimelineValue(Date date) {
        return toTimelineValue(getTime(date));
        //return toTimelineValue(dateDomainValue.getTime());
    }

    /**
     * Translates a value relative to the timeline into a millisecond.
     *
     * @param timelineValue  the timeline value (in milliseconds).
     *
     * @return The domain value (in milliseconds).
     */
    public long toMillisecond(long timelineValue) {

        // calculate the result as if no exceptions
        Segment result = new Segment(this.startTime + timelineValue
                + (timelineValue / this.segmentsIncludedSize)
                * this.segmentsExcludedSize);

        long lastIndex = this.startTime;

        // adjust result for any exceptions in the result calculated
        while (lastIndex <= result.segmentStart) {

            // skip all whole exception segments in the range
            long exceptionSegmentCount;
            while ((exceptionSegmentCount = getExceptionSegmentCount(
                 lastIndex, (result.millisecond / this.segmentSize)
                 * this.segmentSize - 1)) > 0
            ) {
                lastIndex = result.segmentStart;
                // move forward exceptionSegmentCount segments skipping
                // excluded segments
                for (int i = 0; i < exceptionSegmentCount; i++) {
                    do {
                        result.inc();
                    }
                    while (result.inExcludeSegments());
                }
            }
            lastIndex = result.segmentStart;

            // skip exception or excluded segments we may fall on
            while (result.inExceptionSegments() || result.inExcludeSegments()) {
                result.inc();
                lastIndex += this.segmentSize;
            }

            lastIndex++;
        }

        return getTimeFromLong(result.millisecond);
    }

    /**
     * Converts a date/time value to take account of daylight savings time.
     *
     * @param date  the milliseconds.
     *
     * @return The milliseconds.
     */
    public long getTimeFromLong(long date) {
        long result = date;
        if (this.adjustForDaylightSaving) {
            this.workingCalendarNoDST.setTime(new Date(date));
            this.workingCalendar.set(
                this.workingCalendarNoDST.get(Calendar.YEAR),
                this.workingCalendarNoDST.get(Calendar.MONTH),
                this.workingCalendarNoDST.get(Calendar.DATE),
                this.workingCalendarNoDST.get(Calendar.HOUR_OF_DAY),
                this.workingCalendarNoDST.get(Calendar.MINUTE),
                this.workingCalendarNoDST.get(Calendar.SECOND)
            );
            this.workingCalendar.set(Calendar.MILLISECOND,
                    this.workingCalendarNoDST.get(Calendar.MILLISECOND));
            // result = this.workingCalendar.getTimeInMillis();
            // preceding code won't work with JDK 1.3
            result = this.workingCalendar.getTime().getTime();
        }
        return result;
    }

    /**
     * Returns <code>true</code> if a value is contained in the timeline.
     *
     * @param millisecond  the value to verify.
     *
     * @return <code>true</code> if value is contained in the timeline.
     */
    public boolean containsDomainValue(long millisecond) {
        Segment segment = getSegment(millisecond);
        return segment.inIncludeSegments();
    }

    /**
     * Returns <code>true</code> if a value is contained in the timeline.
     *
     * @param date  date to verify
     *
     * @return <code>true</code> if value is contained in the timeline
     */
    public boolean containsDomainValue(Date date) {
        return containsDomainValue(getTime(date));
    }

    /**
     * Returns <code>true</code> if a range of values are contained in the
     * timeline. This is implemented verifying that all segments are in the
     * range.
     *
     * @param domainValueStart start of the range to verify
     * @param domainValueEnd end of the range to verify
     *
     * @return <code>true</code> if the range is contained in the timeline
     */
    public boolean containsDomainRange(long domainValueStart,
                                       long domainValueEnd) {
        if (domainValueEnd < domainValueStart) {
            throw new IllegalArgumentException(
                    "domainValueEnd (" + domainValueEnd
                    + ") < domainValueStart (" + domainValueStart + ")");
        }
        Segment segment = getSegment(domainValueStart);
        boolean contains = true;
        do {
            contains = (segment.inIncludeSegments());
            if (segment.contains(domainValueEnd)) {
                break;
            }
            else {
                segment.inc();
            }
        }
        while (contains);
        return (contains);
    }

    /**
     * Returns <code>true</code> if a range of values are contained in the
     * timeline. This is implemented verifying that all segments are in the
     * range.
     *
     * @param dateDomainValueStart start of the range to verify
     * @param dateDomainValueEnd end of the range to verify
     *
     * @return <code>true</code> if the range is contained in the timeline
     */
    public boolean containsDomainRange(Date dateDomainValueStart,
                                       Date dateDomainValueEnd) {
        return containsDomainRange(getTime(dateDomainValueStart),
                getTime(dateDomainValueEnd));
    }

    /**
     * Adds a segment as an exception. An exception segment is defined as a
     * segment to exclude from what would otherwise be considered a valid
     * segment of the timeline.  An exception segment can not be contained
     * inside an already excluded segment.  If so, no action will occur (the
     * proposed exception segment will be discarded).
     * <p>
     * The segment is identified by a domainValue into any part of the segment.
     * Therefore the segmentStart <= domainValue <= segmentEnd.
     *
     * @param millisecond  domain value to treat as an exception
     */
    public void addException(long millisecond) {
        addException(new Segment(millisecond));
    }

    /**
     * Adds a segment range as an exception. An exception segment is defined as
     * a segment to exclude from what would otherwise be considered a valid
     * segment of the timeline.  An exception segment can not be contained
     * inside an already excluded segment.  If so, no action will occur (the
     * proposed exception segment will be discarded).
     * <p>
     * The segment range is identified by a domainValue that begins a valid
     * segment and ends with a domainValue that ends a valid segment.
     * Therefore the range will contain all segments whose segmentStart
     * <= domainValue and segmentEnd <= toDomainValue.
     *
     * @param fromDomainValue  start of domain range to treat as an exception
     * @param toDomainValue  end of domain range to treat as an exception
     */
    public void addException(long fromDomainValue, long toDomainValue) {
        addException(new SegmentRange(fromDomainValue, toDomainValue));
    }

    /**
     * Adds a segment as an exception. An exception segment is defined as a
     * segment to exclude from what would otherwise be considered a valid
     * segment of the timeline.  An exception segment can not be contained
     * inside an already excluded segment.  If so, no action will occur (the
     * proposed exception segment will be discarded).
     * <p>
     * The segment is identified by a Date into any part of the segment.
     *
     * @param exceptionDate  Date into the segment to exclude.
     */
    public void addException(Date exceptionDate) {
        addException(getTime(exceptionDate));
        //addException(exceptionDate.getTime());
    }

    /**
     * Adds a list of dates as segment exceptions. Each exception segment is
     * defined as a segment to exclude from what would otherwise be considered
     * a valid segment of the timeline.  An exception segment can not be
     * contained inside an already excluded segment.  If so, no action will
     * occur (the proposed exception segment will be discarded).
     * <p>
     * The segment is identified by a Date into any part of the segment.
     *
     * @param exceptionList  List of Date objects that identify the segments to
     *                       exclude.
     */
    public void addExceptions(List exceptionList) {
        for (Iterator iter = exceptionList.iterator(); iter.hasNext();) {
            addException((Date) iter.next());
        }
    }

    /**
     * Adds a segment as an exception. An exception segment is defined as a
     * segment to exclude from what would otherwise be considered a valid
     * segment of the timeline.  An exception segment can not be contained
     * inside an already excluded segment.  This is verified inside this
     * method, and if so, no action will occur (the proposed exception segment
     * will be discarded).
     *
     * @param segment  the segment to exclude.
     */
    private void addException(Segment segment) {
         if (segment.inIncludeSegments()) {
             int p = binarySearchExceptionSegments(segment);
             this.exceptionSegments.add(-(p + 1), segment);
         }
    }

    /**
     * Adds a segment relative to the baseTimeline as an exception. Because a
     * base segment is normally larger than our segments, this may add one or
     * more segment ranges to the exception list.
     * <p>
     * An exception segment is defined as a segment
     * to exclude from what would otherwise be considered a valid segment of
     * the timeline.  An exception segment can not be contained inside an
     * already excluded segment.  If so, no action will occur (the proposed
     * exception segment will be discarded).
     * <p>
     * The segment is identified by a domainValue into any part of the
     * baseTimeline segment.
     *
     * @param domainValue  domain value to teat as a baseTimeline exception.
     */
    public void addBaseTimelineException(long domainValue) {

        Segment baseSegment = this.baseTimeline.getSegment(domainValue);
        if (baseSegment.inIncludeSegments()) {

            // cycle through all the segments contained in the BaseTimeline
            // exception segment
            Segment segment = getSegment(baseSegment.getSegmentStart());
            while (segment.getSegmentStart() <= baseSegment.getSegmentEnd()) {
                if (segment.inIncludeSegments()) {

                    // find all consecutive included segments
                    long fromDomainValue = segment.getSegmentStart();
                    long toDomainValue;
                    do {
                        toDomainValue = segment.getSegmentEnd();
                        segment.inc();
                    }
                    while (segment.inIncludeSegments());

                    // add the interval as an exception
                    addException(fromDomainValue, toDomainValue);

                }
                else {
                    // this is not one of our included segment, skip it
                    segment.inc();
                }
            }
        }
    }

    /**
     * Adds a segment relative to the baseTimeline as an exception. An
     * exception segment is defined as a segment to exclude from what would
     * otherwise be considered a valid segment of the timeline.  An exception
     * segment can not be contained inside an already excluded segment. If so,
     * no action will occure (the proposed exception segment will be discarded).
     * <p>
     * The segment is identified by a domainValue into any part of the segment.
     * Therefore the segmentStart <= domainValue <= segmentEnd.
     *
     * @param date  date domain value to treat as a baseTimeline exception
     */
    public void addBaseTimelineException(Date date) {
        addBaseTimelineException(getTime(date));
    }

    /**
     * Adds all excluded segments from the BaseTimeline as exceptions to our
     * timeline. This allows us to combine two timelines for more complex
     * calculations.
     *
     * @param fromBaseDomainValue Start of the range where exclusions will be
     *                            extracted.
     * @param toBaseDomainValue End of the range to process.
     */
    public void addBaseTimelineExclusions(long fromBaseDomainValue,
                                          long toBaseDomainValue) {

        // find first excluded base segment starting fromDomainValue
        Segment baseSegment = this.baseTimeline.getSegment(fromBaseDomainValue);
        while (baseSegment.getSegmentStart() <= toBaseDomainValue
               && !baseSegment.inExcludeSegments()) {

            baseSegment.inc();

        }

        // cycle over all the base segments groups in the range
        while (baseSegment.getSegmentStart() <= toBaseDomainValue) {

            long baseExclusionRangeEnd = baseSegment.getSegmentStart()
                 + this.baseTimeline.getSegmentsExcluded()
                 * this.baseTimeline.getSegmentSize() - 1;

            // cycle through all the segments contained in the base exclusion
            // area
            Segment segment = getSegment(baseSegment.getSegmentStart());
            while (segment.getSegmentStart() <= baseExclusionRangeEnd) {
                if (segment.inIncludeSegments()) {

                    // find all consecutive included segments
                    long fromDomainValue = segment.getSegmentStart();
                    long toDomainValue;
                    do {
                        toDomainValue = segment.getSegmentEnd();
                        segment.inc();
                    }
                    while (segment.inIncludeSegments());

                    // add the interval as an exception
                    addException(new BaseTimelineSegmentRange(
                            fromDomainValue, toDomainValue));
                }
                else {
                    // this is not one of our included segment, skip it
                    segment.inc();
                }
            }

            // go to next base segment group
            baseSegment.inc(this.baseTimeline.getGroupSegmentCount());
        }
    }

    /**
     * Returns the number of exception segments wholly contained in the
     * (fromDomainValue, toDomainValue) interval.
     *
     * @param fromMillisecond  the beginning of the interval.
     * @param toMillisecond  the end of the interval.
     *
     * @return Number of exception segments contained in the interval.
     */
    public long getExceptionSegmentCount(long fromMillisecond,
                                         long toMillisecond) {
        if (toMillisecond < fromMillisecond) {
            return (0);
        }

        int n = 0;
        for (Iterator iter = this.exceptionSegments.iterator();
             iter.hasNext();) {
            Segment segment = (Segment) iter.next();
            Segment intersection = segment.intersect(fromMillisecond,
                    toMillisecond);
            if (intersection != null) {
                n += intersection.getSegmentCount();
            }
        }

        return (n);
    }

    /**
     * Returns a segment that contains a domainValue. If the domainValue is
     * not contained in the timeline (because it is not contained in the
     * baseTimeline), a Segment that contains
     * <code>index + segmentSize*m</code> will be returned for the smallest
     * <code>m</code> possible.
     *
     * @param millisecond  index into the segment
     *
     * @return A Segment that contains index, or the next possible Segment.
     */
    public Segment getSegment(long millisecond) {
        return new Segment(millisecond);
    }

    /**
     * Returns a segment that contains a date. For accurate calculations,
     * the calendar should use TIME_ZONE for its calculation (or any other
     * similar time zone).
     *
     * If the date is not contained in the timeline (because it is not
     * contained in the baseTimeline), a Segment that contains
     * <code>date + segmentSize*m</code> will be returned for the smallest
     * <code>m</code> possible.
     *
     * @param date date into the segment
     *
     * @return A Segment that contains date, or the next possible Segment.
     */
    public Segment getSegment(Date date) {
        return (getSegment(getTime(date)));
    }

    /**
     * Convenient method to test equality in two objects, taking into account
     * nulls.
     *
     * @param o first object to compare
     * @param p second object to compare
     *
     * @return <code>true</code> if both objects are equal or both
     *         <code>null</code>, <code>false</code> otherwise.
     */
    private boolean equals(Object o, Object p) {
        return (o == p || ((o != null) && o.equals(p)));
    }

    /**
     * Returns true if we are equal to the parameter
     *
     * @param o Object to verify with us
     *
     * @return <code>true</code> or <code>false</code>
     */
    public boolean equals(Object o) {
        if (o instanceof SegmentedTimeline) {
            SegmentedTimeline other = (SegmentedTimeline) o;

            boolean b0 = (this.segmentSize == other.getSegmentSize());
            boolean b1 = (this.segmentsIncluded == other.getSegmentsIncluded());
            boolean b2 = (this.segmentsExcluded == other.getSegmentsExcluded());
            boolean b3 = (this.startTime == other.getStartTime());
            boolean b4 = equals(this.exceptionSegments,
                    other.getExceptionSegments());
            return b0 && b1 && b2 && b3 && b4;
        }
        else {
            return (false);
        }
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 19;
        result = 37 * result
                 + (int) (this.segmentSize ^ (this.segmentSize >>> 32));
        result = 37 * result + (int) (this.startTime ^ (this.startTime >>> 32));
        return result;
    }

    /**
     * Preforms a binary serach in the exceptionSegments sorted array. This
     * array can contain Segments or SegmentRange objects.
     *
     * @param  segment the key to be searched for.
     *
     * @return index of the search segment, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         segment would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt>, if all
     *         elements in the list are less than the specified segment.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    private int binarySearchExceptionSegments(Segment segment) {
        int low = 0;
        int high = this.exceptionSegments.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Segment midSegment = (Segment) this.exceptionSegments.get(mid);

            // first test for equality (contains or contained)
            if (segment.contains(midSegment) || midSegment.contains(segment)) {
                return mid;
            }

            if (midSegment.before(segment)) {
                low = mid + 1;
            }
            else if (midSegment.after(segment)) {
                high = mid - 1;
            }
            else {
                throw new IllegalStateException("Invalid condition.");
            }
        }
        return -(low + 1);  // key not found
    }

    /**
     * Special method that handles conversion between the Default Time Zone and
     * a UTC time zone with no DST. This is needed so all days have the same
     * size. This method is the prefered way of converting a Data into
     * milliseconds for usage in this class.
     *
     * @param date Date to convert to long.
     *
     * @return The milliseconds.
     */
    public long getTime(Date date) {
        long result = date.getTime();
        if (this.adjustForDaylightSaving) {
            this.workingCalendar.setTime(date);
            this.workingCalendarNoDST.set(
                    this.workingCalendar.get(Calendar.YEAR),
                    this.workingCalendar.get(Calendar.MONTH),
                    this.workingCalendar.get(Calendar.DATE),
                    this.workingCalendar.get(Calendar.HOUR_OF_DAY),
                    this.workingCalendar.get(Calendar.MINUTE),
                    this.workingCalendar.get(Calendar.SECOND));
            this.workingCalendarNoDST.set(Calendar.MILLISECOND,
                    this.workingCalendar.get(Calendar.MILLISECOND));
            Date revisedDate = this.workingCalendarNoDST.getTime();
            result = revisedDate.getTime();
        }

        return result;
    }

    /**
     * Converts a millisecond value into a {@link Date} object.
     *
     * @param value  the millisecond value.
     *
     * @return The date.
     */
    public Date getDate(long value) {
        this.workingCalendarNoDST.setTime(new Date(value));
        return (this.workingCalendarNoDST.getTime());
    }

    /**
     * Returns a clone of the timeline.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException ??.
     */
    public Object clone() throws CloneNotSupportedException {
        SegmentedTimeline clone = (SegmentedTimeline) super.clone();
        return clone;
    }

    /**
     * Internal class to represent a valid segment for this timeline. A segment
     * is valid on a timeline if it is part of its included, excluded or
     * exception segments.
     * <p>
     * Each segment will know its segment number, segmentStart, segmentEnd and
     * index inside the segment.
     */
    public class Segment implements Comparable, Cloneable, Serializable {

        /** The segment number. */
        protected long segmentNumber;

        /** The segment start. */
        protected long segmentStart;

        /** The segment end. */
        protected long segmentEnd;

        /** A reference point within the segment. */
        protected long millisecond;

        /**
         * Protected constructor only used by sub-classes.
         */
        protected Segment() {
            // empty
        }

        /**
         * Creates a segment for a given point in time.
         *
         * @param millisecond  the millisecond (as encoded by java.util.Date).
         */
        protected Segment(long millisecond) {
            this.segmentNumber = calculateSegmentNumber(millisecond);
            this.segmentStart = SegmentedTimeline.this.startTime
                + this.segmentNumber * SegmentedTimeline.this.segmentSize;
            this.segmentEnd
                = this.segmentStart + SegmentedTimeline.this.segmentSize - 1;
            this.millisecond = millisecond;
        }

        /**
         * Calculates the segment number for a given millisecond.
         *
         * @param millis  the millisecond (as encoded by java.util.Date).
         *
         * @return The segment number.
         */
        public long calculateSegmentNumber(long millis) {
            if (millis >= SegmentedTimeline.this.startTime) {
                return (millis - SegmentedTimeline.this.startTime)
                    / SegmentedTimeline.this.segmentSize;
            }
            else {
                return ((millis - SegmentedTimeline.this.startTime)
                    / SegmentedTimeline.this.segmentSize) - 1;
            }
        }

        /**
         * Returns the segment number of this segment. Segments start at 0.
         *
         * @return The segment number.
         */
        public long getSegmentNumber() {
            return this.segmentNumber;
        }

        /**
         * Returns always one (the number of segments contained in this
         * segment).
         *
         * @return The segment count (always 1 for this class).
         */
        public long getSegmentCount() {
            return 1;
        }

        /**
         * Gets the start of this segment in ms.
         *
         * @return The segment start.
         */
        public long getSegmentStart() {
            return this.segmentStart;
        }

        /**
         * Gets the end of this segment in ms.
         *
         * @return The segment end.
         */
        public long getSegmentEnd() {
            return this.segmentEnd;
        }

        /**
         * Returns the millisecond used to reference this segment (always
         * between the segmentStart and segmentEnd).
         *
         * @return The millisecond.
         */
        public long getMillisecond() {
            return this.millisecond;
        }

        /**
         * Returns a {@link java.util.Date} that represents the reference point
         * for this segment.
         *
         * @return The date.
         */
        public Date getDate() {
            return SegmentedTimeline.this.getDate(this.millisecond);
        }

        /**
         * Returns true if a particular millisecond is contained in this
         * segment.
         *
         * @param millis  the millisecond to verify.
         *
         * @return <code>true</code> if the millisecond is contained in the
         *         segment.
         */
        public boolean contains(long millis) {
            return (this.segmentStart <= millis && millis <= this.segmentEnd);
        }

        /**
         * Returns <code>true</code> if an interval is contained in this
         * segment.
         *
         * @param from  the start of the interval.
         * @param to  the end of the interval.
         *
         * @return <code>true</code> if the interval is contained in the
         *         segment.
         */
        public boolean contains(long from, long to) {
            return (this.segmentStart <= from && to <= this.segmentEnd);
        }

        /**
         * Returns <code>true</code> if a segment is contained in this segment.
         *
         * @param segment  the segment to test for inclusion
         *
         * @return <code>true</code> if the segment is contained in this
         *         segment.
         */
        public boolean contains(Segment segment) {
            return contains(segment.getSegmentStart(), segment.getSegmentEnd());
        }

        /**
         * Returns <code>true</code> if this segment is contained in an
         * interval.
         *
         * @param from  the start of the interval.
         * @param to  the end of the interval.
         *
         * @return <code>true</code> if this segment is contained in the
         *         interval.
         */
        public boolean contained(long from, long to) {
            return (from <= this.segmentStart && this.segmentEnd <= to);
        }

        /**
         * Returns a segment that is the intersection of this segment and the
         * interval.
         *
         * @param from  the start of the interval.
         * @param to  the end of the interval.
         *
         * @return A segment.
         */
        public Segment intersect(long from, long to) {
            if (from <= this.segmentStart && this.segmentEnd <= to) {
                return this;
            }
            else {
                return null;
            }
        }

        /**
         * Returns <code>true</code> if this segment is wholly before another
         * segment.
         *
         * @param other  the other segment.
         *
         * @return A boolean.
         */
        public boolean before(Segment other) {
            return (this.segmentEnd < other.getSegmentStart());
        }

        /**
         * Returns <code>true</code> if this segment is wholly after another
         * segment.
         *
         * @param other  the other segment.
         *
         * @return A boolean.
         */
        public boolean after(Segment other) {
            return (this.segmentStart > other.getSegmentEnd());
        }

        /**
         * Tests an object (usually another <code>Segment</code>) for equality
         * with this segment.
         *
         * @param object The other segment to compare with us
         *
         * @return <code>true</code> if we are the same segment
         */
        public boolean equals(Object object) {
            if (object instanceof Segment) {
                Segment other = (Segment) object;
                return (this.segmentNumber == other.getSegmentNumber()
                        && this.segmentStart == other.getSegmentStart()
                        && this.segmentEnd == other.getSegmentEnd()
                        && this.millisecond == other.getMillisecond());
            }
            else {
                return false;
            }
        }

        /**
         * Returns a copy of ourselves or <code>null</code> if there was an
         * exception during cloning.
         *
         * @return A copy of this segment.
         */
        public Segment copy() {
            try {
                return (Segment) this.clone();
            }
            catch (CloneNotSupportedException e) {
                return null;
            }
        }

        /**
         * Will compare this Segment with another Segment (from Comparable
         * interface).
         *
         * @param object The other Segment to compare with
         *
         * @return -1: this < object, 0: this.equal(object) and
         *         +1: this > object
         */
        public int compareTo(Object object) {
            Segment other = (Segment) object;
            if (this.before(other)) {
                return -1;
            }
            else if (this.after(other)) {
                return +1;
            }
            else {
                return 0;
            }
        }

        /**
         * Returns true if we are an included segment and we are not an
         * exception.
         *
         * @return <code>true</code> or <code>false</code>.
         */
        public boolean inIncludeSegments() {
            if (getSegmentNumberRelativeToGroup()
                    < SegmentedTimeline.this.segmentsIncluded) {
                return !inExceptionSegments();
            }
            else {
                return false;
            }
        }

        /**
         * Returns true if we are an excluded segment.
         *
         * @return <code>true</code> or <code>false</code>.
         */
        public boolean inExcludeSegments() {
            return getSegmentNumberRelativeToGroup()
                    >= SegmentedTimeline.this.segmentsIncluded;
        }

        /**
         * Calculate the segment number relative to the segment group. This
         * will be a number between 0 and segmentsGroup-1. This value is
         * calculated from the segmentNumber. Special care is taken for
         * negative segmentNumbers.
         *
         * @return The segment number.
         */
        private long getSegmentNumberRelativeToGroup() {
            long p = (this.segmentNumber
                    % SegmentedTimeline.this.groupSegmentCount);
            if (p < 0) {
                p += SegmentedTimeline.this.groupSegmentCount;
            }
            return p;
        }

        /**
         * Returns true if we are an exception segment. This is implemented via
         * a binary search on the exceptionSegments sorted list.
         *
         * If the segment is not listed as an exception in our list and we have
         * a baseTimeline, a check is performed to see if the segment is inside
         * an excluded segment from our base. If so, it is also considered an
         * exception.
         *
         * @return <code>true</code> if we are an exception segment.
         */
        public boolean inExceptionSegments() {
            return binarySearchExceptionSegments(this) >= 0;
        }

        /**
         * Increments the internal attributes of this segment by a number of
         * segments.
         *
         * @param n Number of segments to increment.
         */
        public void inc(long n) {
            this.segmentNumber += n;
            long m = n * SegmentedTimeline.this.segmentSize;
            this.segmentStart += m;
            this.segmentEnd += m;
            this.millisecond += m;
        }

        /**
         * Increments the internal attributes of this segment by one segment.
         * The exact time incremented is segmentSize.
         */
        public void inc() {
            inc(1);
        }

        /**
         * Decrements the internal attributes of this segment by a number of
         * segments.
         *
         * @param n Number of segments to decrement.
         */
        public void dec(long n) {
            this.segmentNumber -= n;
            long m = n * SegmentedTimeline.this.segmentSize;
            this.segmentStart -= m;
            this.segmentEnd -= m;
            this.millisecond -= m;
        }

        /**
         * Decrements the internal attributes of this segment by one segment.
         * The exact time decremented is segmentSize.
         */
        public void dec() {
            dec(1);
        }

        /**
         * Moves the index of this segment to the beginning if the segment.
         */
        public void moveIndexToStart() {
            this.millisecond = this.segmentStart;
        }

        /**
         * Moves the index of this segment to the end of the segment.
         */
        public void moveIndexToEnd() {
            this.millisecond = this.segmentEnd;
        }

    }

    /**
     * Private internal class to represent a range of segments. This class is
     * mainly used to store in one object a range of exception segments. This
     * optimizes certain timelines that use a small segment size (like an
     * intraday timeline) allowing them to express a day exception as one
     * SegmentRange instead of multi Segments.
     */
    protected class SegmentRange extends Segment {

        /** The number of segments in the range. */
        private long segmentCount;

        /**
         * Creates a SegmentRange between a start and end domain values.
         *
         * @param fromMillisecond  start of the range
         * @param toMillisecond  end of the range
         */
        public SegmentRange(long fromMillisecond, long toMillisecond) {

            Segment start = getSegment(fromMillisecond);
            Segment end = getSegment(toMillisecond);
//            if (start.getSegmentStart() != fromMillisecond
//                || end.getSegmentEnd() != toMillisecond) {
//                throw new IllegalArgumentException("Invalid Segment Range ["
//                    + fromMillisecond + "," + toMillisecond + "]");
//            }

            this.millisecond = fromMillisecond;
            this.segmentNumber = calculateSegmentNumber(fromMillisecond);
            this.segmentStart = start.segmentStart;
            this.segmentEnd = end.segmentEnd;
            this.segmentCount
                = (end.getSegmentNumber() - start.getSegmentNumber() + 1);
        }

        /**
         * Returns the number of segments contained in this range.
         *
         * @return The segment count.
         */
        public long getSegmentCount() {
            return this.segmentCount;
        }

        /**
         * Returns a segment that is the intersection of this segment and the
         * interval.
         *
         * @param from  the start of the interval.
         * @param to  the end of the interval.
         *
         * @return The intersection.
         */
        public Segment intersect(long from, long to) {

            // Segment fromSegment = getSegment(from);
            // fromSegment.inc();
            // Segment toSegment = getSegment(to);
            // toSegment.dec();
            long start = Math.max(from, this.segmentStart);
            long end = Math.min(to, this.segmentEnd);
            // long start = Math.max(
            //     fromSegment.getSegmentStart(), this.segmentStart
            // );
            // long end = Math.min(toSegment.getSegmentEnd(), this.segmentEnd);
            if (start <= end) {
                return new SegmentRange(start, end);
            }
            else {
                return null;
            }
        }

        /**
         * Returns true if all Segments of this SegmentRenge are an included
         * segment and are not an exception.
         *
         * @return <code>true</code> or </code>false</code>.
         */
        public boolean inIncludeSegments() {
            for (Segment segment = getSegment(this.segmentStart);
                segment.getSegmentStart() < this.segmentEnd;
                segment.inc()) {
                if (!segment.inIncludeSegments()) {
                    return (false);
                }
            }
            return true;
        }

        /**
         * Returns true if we are an excluded segment.
         *
         * @return <code>true</code> or </code>false</code>.
         */
        public boolean inExcludeSegments() {
            for (Segment segment = getSegment(this.segmentStart);
                segment.getSegmentStart() < this.segmentEnd;
                segment.inc()) {
                if (!segment.inExceptionSegments()) {
                    return (false);
                }
            }
            return true;
        }

        /**
         * Not implemented for SegmentRange. Always throws
         * IllegalArgumentException.
         *
         * @param n Number of segments to increment.
         */
        public void inc(long n) {
            throw new IllegalArgumentException(
                    "Not implemented in SegmentRange");
        }

    }

    /**
     * Special <code>SegmentRange</code> that came from the BaseTimeline.
     */
    protected class BaseTimelineSegmentRange extends SegmentRange {

        /**
         * Constructor.
         *
         * @param fromDomainValue  the start value.
         * @param toDomainValue  the end value.
         */
        public BaseTimelineSegmentRange(long fromDomainValue,
                                        long toDomainValue) {
            super(fromDomainValue, toDomainValue);
        }

    }

}
