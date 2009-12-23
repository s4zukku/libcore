/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// BEGIN android-note
// changed from ICU to resource bundles
// END android-note

package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Currency;
import java.util.Locale;
// BEGIN android-added
import java.util.ResourceBundle;
// END android-added

import com.ibm.icu4jni.util.LocaleData;
import org.apache.harmony.text.internal.nls.Messages;

/**
 * The abstract base class for all number formats. This class provides the
 * interface for formatting and parsing numbers. {@code NumberFormat} also
 * provides methods for determining which locales have number formats, and what
 * their names are.
 * <p>
 * {@code NumberFormat} helps you to format and parse numbers for any locale.
 * Your code can be completely independent of the locale conventions for decimal
 * points, thousands-separators, or even the particular decimal digits used, or
 * whether the number format is even decimal.
 * <p>
 * To format a number for the current locale, use one of the factory class
 * methods:
 * <blockquote>
 *
 * <pre>
 * myString = NumberFormat.getInstance().format(myNumber);
 * </pre>
 *
 * </blockquote>
 * <p>
 * If you are formatting multiple numbers, it is more efficient to get the
 * format and use it multiple times so that the system doesn't have to fetch the
 * information about the local language and country conventions multiple times.
 * <blockquote>
 *
 * <pre>
 * NumberFormat nf = NumberFormat.getInstance();
 * for (int i = 0; i &lt; a.length; ++i) {
 *     output.println(nf.format(myNumber[i]) + &quot;; &quot;);
 * }
 * </pre>
 *
 * </blockquote>
 * <p>
 * To format a number for a different locale, specify it in the call to
 * {@code getInstance}.
 * <blockquote>
 *
 * <pre>
 * NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
 * </pre>
 *
 * </blockquote>
 * <p>
 * You can also use a {@code NumberFormat} to parse numbers:
 * <blockquote>
 *
 * <pre>
 * myNumber = nf.parse(myString);
 * </pre>
 *
 * </blockquote>
 * <p>
 * Use {@code getInstance} or {@code getNumberInstance} to get the normal number
 * format. Use {@code getIntegerInstance} to get an integer number format. Use
 * {@code getCurrencyInstance} to get the currency number format and use
 * {@code getPercentInstance} to get a format for displaying percentages. With
 * this format, a fraction like 0.53 is displayed as 53%.
 * <p>
 * You can also control the display of numbers with methods such as
 * {@code setMinimumFractionDigits}. If you want even more control over the
 * format or parsing, or want to give your users more control, you can try
 * casting the {@code NumberFormat} you get from the factory methods to a
 * {@code DecimalFormat}. This will work for the vast majority of locales; just
 * remember to put it in a {@code try} block in case you encounter an unusual
 * one.
 * <p>
 * {@code NumberFormat} is designed such that some controls work for formatting
 * and others work for parsing. For example, {@code setParseIntegerOnly} only
 * affects parsing: If set to {@code true}, "3456.78" is parsed as 3456 (and
 * leaves the parse position just after '6'); if set to {@code false},
 * "3456.78" is parsed as 3456.78 (and leaves the parse position just after
 * '8'). This is independent of formatting.
 * <p>
 * You can also use forms of the {@code parse} and {@code format} methods with
 * {@code ParsePosition} and {@code FieldPosition} to allow you to:
 * <ul>
 * <li>progressively parse through pieces of a string;</li>
 * <li>align the decimal point and other areas.</li>
 * </ul>
 * For example, you can align numbers in two ways:
 * <ol>
 * <li> If you are using a monospaced font with spacing for alignment, you can
 * pass the {@code FieldPosition} in your format call, with {@code field} =
 * {@code INTEGER_FIELD}. On output, {@code getEndIndex} will be set to the
 * offset between the last character of the integer and the decimal. Add
 * (desiredSpaceCount - getEndIndex) spaces to the front of the string.</li>
 * <li> If you are using proportional fonts, instead of padding with spaces,
 * measure the width of the string in pixels from the start to
 * {@code getEndIndex}. Then move the pen by (desiredPixelWidth -
 * widthToAlignmentPoint) before drawing the text. This also works where there
 * is no decimal but possibly additional characters before or after the number,
 * for example with parentheses in negative numbers: "(12)" for -12.</li>
 * </ol>
 * <h4>Synchronization</h4>
 * <p>
 * Number formats are generally not synchronized. It is recommended to create
 * separate format instances for each thread. If multiple threads access a
 * format concurrently, it must be synchronized externally.
 * <p>
 * <h4>DecimalFormat</h4>
 * <p>
 * {@code DecimalFormat} is the concrete implementation of {@code NumberFormat},
 * and the {@code NumberFormat} API is essentially an abstraction of
 * {@code DecimalFormat's} API. Refer to {@code DecimalFormat} for more
 * information about this API.
 *
 * @see DecimalFormat
 * @see java.text.ChoiceFormat
 */
public abstract class NumberFormat extends Format {

    private static final long serialVersionUID = -2308460125733713944L;

    /**
     * Field constant identifying the integer part of a number.
     */
    public static final int INTEGER_FIELD = 0;

    /**
     * Field constant identifying the fractional part of a number.
     */
    public static final int FRACTION_FIELD = 1;

    private boolean groupingUsed = true, parseIntegerOnly = false;

    private int maximumIntegerDigits = 40, minimumIntegerDigits = 1,
            maximumFractionDigits = 3, minimumFractionDigits = 0;

    /**
     * Constructs a new instance of {@code NumberFormat}.
     */
    public NumberFormat() {
    }

    /**
     * Returns a new {@code NumberFormat} with the same properties as this
     * {@code NumberFormat}.
     * 
     * @return a shallow copy of this {@code NumberFormat}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        return super.clone();
    }

    /**
     * Compares the specified object to this number format and indicates if
     * they are equal. In order to be equal, {@code object} must be an instance
     * of {@code NumberFormat} with the same pattern and properties.
     * 
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this number
     *         format; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof NumberFormat)) {
            return false;
        }
        NumberFormat obj = (NumberFormat) object;
        return groupingUsed == obj.groupingUsed
                && parseIntegerOnly == obj.parseIntegerOnly
                && maximumFractionDigits == obj.maximumFractionDigits
                && maximumIntegerDigits == obj.maximumIntegerDigits
                && minimumFractionDigits == obj.minimumFractionDigits
                && minimumIntegerDigits == obj.minimumIntegerDigits;
    }

    /**
     * Formats the specified double using the rules of this number format.
     * 
     * @param value
     *            the double to format.
     * @return the formatted string.
     */
    public final String format(double value) {
        return format(value, new StringBuffer(), new FieldPosition(0))
                .toString();
    }

    /**
     * Formats the specified double value as a string using the pattern of this
     * number format and appends the string to the specified string buffer.
     * <p>
     * If the {@code field} member of {@code position} contains a value
     * specifying a format field, then its {@code beginIndex} and
     * {@code endIndex} members will be updated with the position of the first
     * occurrence of this field in the formatted text.
     *
     * @param value
     *            the double to format.
     * @param buffer
     *            the target string buffer to append the formatted double value
     *            to.
     * @param field
     *            on input: an optional alignment field; on output: the offsets
     *            of the alignment field in the formatted text.
     * @return the string buffer.
     */
    public abstract StringBuffer format(double value, StringBuffer buffer,
            FieldPosition field);

    /**
     * Formats the specified long using the rules of this number format.
     * 
     * @param value
     *            the long to format.
     * @return the formatted string.
     */
    public final String format(long value) {
        return format(value, new StringBuffer(), new FieldPosition(0))
                .toString();
    }

    /**
     * Formats the specified long value as a string using the pattern of this
     * number format and appends the string to the specified string buffer.
     * <p>
     * If the {@code field} member of {@code position} contains a value
     * specifying a format field, then its {@code beginIndex} and
     * {@code endIndex} members will be updated with the position of the first
     * occurrence of this field in the formatted text.
     *
     * @param value
     *            the long to format.
     * @param buffer
     *            the target string buffer to append the formatted long value
     *            to.
     * @param field
     *            on input: an optional alignment field; on output: the offsets
     *            of the alignment field in the formatted text.
     * @return the string buffer.
     */
    public abstract StringBuffer format(long value, StringBuffer buffer,
            FieldPosition field);

    /**
     * Formats the specified object as a string using the pattern of this number
     * format and appends the string to the specified string buffer.
     * <p>
     * If the {@code field} member of {@code field} contains a value specifying
     * a format field, then its {@code beginIndex} and {@code endIndex} members
     * will be updated with the position of the first occurrence of this field
     * in the formatted text.
     *
     * @param object
     *            the object to format, must be a {@code Number}.
     * @param buffer
     *            the target string buffer to append the formatted number to.
     * @param field
     *            on input: an optional alignment field; on output: the offsets
     *            of the alignment field in the formatted text.
     * @return the string buffer.
     * @throws IllegalArgumentException
     *             if {@code object} is not an instance of {@code Number}.
     */
    @Override
    public StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
        if (object instanceof Double || object instanceof Float) {
            double dv = ((Number) object).doubleValue();
            return format(dv, buffer, field);
        } else if (object instanceof Number) {
            long lv = ((Number) object).longValue();
            return format(lv, buffer, field);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Gets the list of installed locales which support {@code NumberFormat}.
     * 
     * @return an array of locales.
     */
    public static Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    /**
     * Returns the currency used by this number format.
     * <p>
     * This implementation throws {@code UnsupportedOperationException},
     * concrete subclasses should override this method if they support currency
     * formatting.
     * <p>
     * 
     * @return the currency that was set in getInstance() or in setCurrency(),
     *         or {@code null}.
     * @throws UnsupportedOperationException
     */
    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing currency values
     * for the default locale.
     * 
     * @return a {@code NumberFormat} for handling currency values.
     */
    public final static NumberFormat getCurrencyInstance() {
        return getCurrencyInstance(Locale.getDefault());
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing currency values
     * for the specified locale.
     * 
     * @param locale
     *            the locale to use.
     * @return a {@code NumberFormat} for handling currency values.
     */
    public static NumberFormat getCurrencyInstance(Locale locale) {
        // BEGIN android-changed
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(locale, localeData.currencyPattern);
        // END android-changed
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing integers for the
     * default locale.
     * 
     * @return a {@code NumberFormat} for handling integers.
     */
    public final static NumberFormat getIntegerInstance() {
        return getIntegerInstance(Locale.getDefault());
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing integers for
     * the specified locale.
     * 
     * @param locale
     *            the locale to use.
     * @return a {@code NumberFormat} for handling integers.
     */
    public static NumberFormat getIntegerInstance(Locale locale) {
        // BEGIN android-changed
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        NumberFormat result = getInstance(locale, localeData.integerPattern);
        result.setParseIntegerOnly(true);
        return result;
        // END android-changed
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing numbers for the
     * default locale.
     * 
     * @return a {@code NumberFormat} for handling {@code Number} objects.
     */
    public final static NumberFormat getInstance() {
        return getNumberInstance();
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing numbers for the
     * specified locale.
     * 
     * @param locale
     *            the locale to use.
     * @return a {@code NumberFormat} for handling {@code Number} objects.
     */
    public static NumberFormat getInstance(Locale locale) {
        return getNumberInstance(locale);
    }

    // BEGIN android-added
    private static NumberFormat getInstance(Locale locale, String pattern) {
        return new DecimalFormat(pattern, new DecimalFormatSymbols(locale));
    }
    // END android-added

    /**
     * Returns the maximum number of fraction digits that are printed when
     * formatting. If the maximum is less than the number of fraction digits,
     * the least significant digits are truncated.
     * 
     * @return the maximum number of fraction digits.
     */
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * Returns the maximum number of integer digits that are printed when
     * formatting. If the maximum is less than the number of integer digits, the
     * most significant digits are truncated.
     * 
     * @return the maximum number of integer digits.
     */
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }

    /**
     * Returns the minimum number of fraction digits that are printed when
     * formatting.
     * 
     * @return the minimum number of fraction digits.
     */
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * Returns the minimum number of integer digits that are printed when
     * formatting.
     * 
     * @return the minimum number of integer digits.
     */
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing numbers for the
     * default locale.
     * 
     * @return a {@code NumberFormat} for handling {@code Number} objects.
     */
    public final static NumberFormat getNumberInstance() {
        return getNumberInstance(Locale.getDefault());
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing numbers for the
     * specified locale.
     * 
     * @param locale
     *            the locale to use.
     * @return a {@code NumberFormat} for handling {@code Number} objects.
     */
    public static NumberFormat getNumberInstance(Locale locale) {
        // BEGIN android-changed
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(locale, localeData.numberPattern);
        // END android-changed
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing percentage
     * values for the default locale.
     * 
     * @return a {@code NumberFormat} for handling percentage values.
     */
    public final static NumberFormat getPercentInstance() {
        return getPercentInstance(Locale.getDefault());
    }

    /**
     * Returns a {@code NumberFormat} for formatting and parsing percentage
     * values for the specified locale.
     * 
     * @param locale
     *            the locale to use.
     * @return a {@code NumberFormat} for handling percentage values.
     */
    public static NumberFormat getPercentInstance(Locale locale) {
        // BEGIN android-changed
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(locale, localeData.percentPattern);
        // END android-changed
    }

    @Override
    public int hashCode() {
        return (groupingUsed ? 1231 : 1237) + (parseIntegerOnly ? 1231 : 1237)
                + maximumFractionDigits + maximumIntegerDigits
                + minimumFractionDigits + minimumIntegerDigits;
    }

    /**
     * Indicates whether this number format formats and parses numbers using a
     * grouping separator.
     * 
     * @return {@code true} if a grouping separator is used; {@code false}
     *         otherwise.
     */
    public boolean isGroupingUsed() {
        return groupingUsed;
    }

    /**
     * Indicates whether this number format only parses integer numbers. Parsing
     * stops if a decimal separator is encountered.
     * 
     * @return {@code true} if this number format only parses integers,
     *         {@code false} if if parsese integers as well as fractions.
     */
    public boolean isParseIntegerOnly() {
        return parseIntegerOnly;
    }

    /**
     * Parses a {@code Number} from the specified string using the rules of this
     * number format.
     * 
     * @param string
     *            the string to parse.
     * @return the {@code Number} resulting from the parsing.
     * @throws ParseException
     *            if an error occurs during parsing.
     */
    public Number parse(String string) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Number number = parse(string, pos);
        if (pos.getIndex() == 0) {
            // text.1D=Unparseable number: {0}
            throw new ParseException(
                    Messages.getString("text.1D", string), pos.getErrorIndex()); //$NON-NLS-1$
        }
        return number;
    }

    /**
     * Parses a {@code Number} from the specified string starting at the index
     * specified by {@code position}. If the string is successfully parsed then
     * the index of the {@code ParsePosition} is updated to the index following
     * the parsed text. On error, the index is unchanged and the error index of
     * {@code ParsePosition} is set to the index where the error occurred.
     * 
     * @param string
     *            the string to parse.
     * @param position
     *            input/output parameter, specifies the start index in
     *            {@code string} from where to start parsing. If parsing is
     *            successful, it is updated with the index following the parsed
     *            text; on error, the index is unchanged and the error index is
     *            set to the index where the error occurred.
     * @return the {@code Number} resulting from the parse or {@code null} if
     *         there is an error.
     */
    public abstract Number parse(String string, ParsePosition position);

    @Override
    public final Object parseObject(String string, ParsePosition position) {
        if (position == null) {
            // text.1A=position is null
            throw new NullPointerException(Messages.getString("text.1A")); //$NON-NLS-1$
        }

        try {
            return parse(string, position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the currency used by this number format when formatting currency
     * values. The min and max fraction digits remain the same.
     * <p>
     * This implementation throws {@code UnsupportedOperationException},
     * concrete subclasses should override this method if they support currency
     * formatting.
     *
     * @param currency
     *            the new currency.
     * @throws UnsupportedOperationException
     */
    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets whether this number format formats and parses numbers using a
     * grouping separator.
     * 
     * @param value
     *            {@code true} if a grouping separator is used; {@code false}
     *            otherwise.
     */
    public void setGroupingUsed(boolean value) {
        groupingUsed = value;
    }

    /**
     * Sets the maximum number of fraction digits that are printed when
     * formatting. If the maximum is less than the number of fraction digits,
     * the least significant digits are truncated.
     * 
     * @param value
     *            the maximum number of fraction digits.
     */
    public void setMaximumFractionDigits(int value) {
        maximumFractionDigits = value < 0 ? 0 : value;
        if (maximumFractionDigits < minimumFractionDigits) {
            minimumFractionDigits = maximumFractionDigits;
        }
    }

    /**
     * Sets the new maximum count of integer digits that are printed when
     * formatting. If the maximum is less than the number of integer digits, the
     * most significant digits are truncated.
     * 
     * @param value
     *            the new maximum number of integer numerals for display.
     */
    public void setMaximumIntegerDigits(int value) {
        maximumIntegerDigits = value < 0 ? 0 : value;
        if (maximumIntegerDigits < minimumIntegerDigits) {
            minimumIntegerDigits = maximumIntegerDigits;
        }
    }

    /**
     * Sets the minimum number of fraction digits that are printed when
     * formatting.
     * 
     * @param value
     *            the minimum number of fraction digits.
     */
    public void setMinimumFractionDigits(int value) {
        minimumFractionDigits = value < 0 ? 0 : value;
        if (maximumFractionDigits < minimumFractionDigits) {
            maximumFractionDigits = minimumFractionDigits;
        }
    }

    /**
     * Sets the minimum number of integer digits that are printed when
     * formatting.
     * 
     * @param value
     *            the minimum number of integer digits.
     */
    public void setMinimumIntegerDigits(int value) {
        minimumIntegerDigits = value < 0 ? 0 : value;
        if (maximumIntegerDigits < minimumIntegerDigits) {
            maximumIntegerDigits = minimumIntegerDigits;
        }
    }

    /**
     * Specifies if this number format should parse numbers only as integers or
     * else as any kind of number. If this method is called with a {@code true}
     * value then subsequent parsing attempts will stop if a decimal separator
     * is encountered.
     * 
     * @param value
     *            {@code true} to only parse integers, {@code false} to parse
     *            integers as well as fractions.
     */
    public void setParseIntegerOnly(boolean value) {
        parseIntegerOnly = value;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("groupingUsed", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("maxFractionDigits", Byte.TYPE), //$NON-NLS-1$
            new ObjectStreamField("maximumFractionDigits", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("maximumIntegerDigits", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("maxIntegerDigits", Byte.TYPE), //$NON-NLS-1$
            new ObjectStreamField("minFractionDigits", Byte.TYPE), //$NON-NLS-1$
            new ObjectStreamField("minimumFractionDigits", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("minimumIntegerDigits", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("minIntegerDigits", Byte.TYPE), //$NON-NLS-1$
            new ObjectStreamField("parseIntegerOnly", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("groupingUsed", groupingUsed); //$NON-NLS-1$
        fields
                .put(
                        "maxFractionDigits", //$NON-NLS-1$
                        maximumFractionDigits < Byte.MAX_VALUE ? (byte) maximumFractionDigits
                                : Byte.MAX_VALUE);
        fields.put("maximumFractionDigits", maximumFractionDigits); //$NON-NLS-1$
        fields.put("maximumIntegerDigits", maximumIntegerDigits); //$NON-NLS-1$
        fields
                .put(
                        "maxIntegerDigits", //$NON-NLS-1$
                        maximumIntegerDigits < Byte.MAX_VALUE ? (byte) maximumIntegerDigits
                                : Byte.MAX_VALUE);
        fields
                .put(
                        "minFractionDigits", //$NON-NLS-1$
                        minimumFractionDigits < Byte.MAX_VALUE ? (byte) minimumFractionDigits
                                : Byte.MAX_VALUE);
        fields.put("minimumFractionDigits", minimumFractionDigits); //$NON-NLS-1$
        fields.put("minimumIntegerDigits", minimumIntegerDigits); //$NON-NLS-1$
        fields
                .put(
                        "minIntegerDigits", //$NON-NLS-1$
                        minimumIntegerDigits < Byte.MAX_VALUE ? (byte) minimumIntegerDigits
                                : Byte.MAX_VALUE);
        fields.put("parseIntegerOnly", parseIntegerOnly); //$NON-NLS-1$
        fields.put("serialVersionOnStream", 1); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        groupingUsed = fields.get("groupingUsed", true); //$NON-NLS-1$
        parseIntegerOnly = fields.get("parseIntegerOnly", false); //$NON-NLS-1$
        if (fields.get("serialVersionOnStream", 0) == 0) { //$NON-NLS-1$
            maximumFractionDigits = fields.get("maxFractionDigits", (byte) 3); //$NON-NLS-1$
            maximumIntegerDigits = fields.get("maxIntegerDigits", (byte) 40); //$NON-NLS-1$
            minimumFractionDigits = fields.get("minFractionDigits", (byte) 0); //$NON-NLS-1$
            minimumIntegerDigits = fields.get("minIntegerDigits", (byte) 1); //$NON-NLS-1$
        } else {
            maximumFractionDigits = fields.get("maximumFractionDigits", 3); //$NON-NLS-1$
            maximumIntegerDigits = fields.get("maximumIntegerDigits", 40); //$NON-NLS-1$
            minimumFractionDigits = fields.get("minimumFractionDigits", 0); //$NON-NLS-1$
            minimumIntegerDigits = fields.get("minimumIntegerDigits", 1); //$NON-NLS-1$
        }
        if (minimumIntegerDigits > maximumIntegerDigits
                || minimumFractionDigits > maximumFractionDigits) {
            // text.00=min digits greater than max digits
            throw new InvalidObjectException(Messages.getString("text.00")); //$NON-NLS-1$
        }
        if (minimumIntegerDigits < 0 || maximumIntegerDigits < 0
                || minimumFractionDigits < 0 || maximumFractionDigits < 0) {
            // text.01=min or max digits negative
            throw new InvalidObjectException(Messages.getString("text.01")); //$NON-NLS-1$
        }
    }

    /**
     * The instances of this inner class are used as attribute keys and values
     * in {@code AttributedCharacterIterator} that the
     * {@link NumberFormat#formatToCharacterIterator(Object)} method returns.
     * <p>
     * There is no public constructor in this class, the only instances are the
     * constants defined here.
     * <p>
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = 7494728892700160890L;

        /**
         * This constant stands for the number sign.
         */
        public static final Field SIGN = new Field("sign"); //$NON-NLS-1$

        /**
         * This constant stands for the integer part of the number.
         */
        public static final Field INTEGER = new Field("integer"); //$NON-NLS-1$

        /**
         * This constant stands for the fraction part of the number.
         */
        public static final Field FRACTION = new Field("fraction"); //$NON-NLS-1$

        /**
         * This constant stands for the exponent part of the number.
         */
        public static final Field EXPONENT = new Field("exponent"); //$NON-NLS-1$

        /**
         * This constant stands for the exponent sign symbol.
         */
        public static final Field EXPONENT_SIGN = new Field("exponent sign"); //$NON-NLS-1$

        /**
         * This constant stands for the exponent symbol.
         */
        public static final Field EXPONENT_SYMBOL = new Field("exponent symbol"); //$NON-NLS-1$

        /**
         * This constant stands for the decimal separator.
         */
        public static final Field DECIMAL_SEPARATOR = new Field(
                "decimal separator"); //$NON-NLS-1$

        /**
         * This constant stands for the grouping separator.
         */
        public static final Field GROUPING_SEPARATOR = new Field(
                "grouping separator"); //$NON-NLS-1$

        /**
         * This constant stands for the percent symbol.
         */
        public static final Field PERCENT = new Field("percent"); //$NON-NLS-1$

        /**
         * This constant stands for the permille symbol.
         */
        public static final Field PERMILLE = new Field("per mille"); //$NON-NLS-1$

        /**
         * This constant stands for the currency symbol.
         */
        public static final Field CURRENCY = new Field("currency"); //$NON-NLS-1$

        /**
         * Constructs a new instance of {@code NumberFormat.Field} with the
         * given field name.
         *
         * @param fieldName
         *            the field name.
         */
        protected Field(String fieldName) {
            super(fieldName);
        }

        /**
         * Resolves instances that are deserialized to the constant
         * {@code NumberFormat.Field} values.
         *
         * @return the resolved field object.
         * @throws InvalidObjectException
         *             if an error occurs while resolving the field object.
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.equals(INTEGER)) {
                return INTEGER;
            }
            if (this.equals(FRACTION)) {
                return FRACTION;
            }
            if (this.equals(EXPONENT)) {
                return EXPONENT;
            }
            if (this.equals(EXPONENT_SIGN)) {
                return EXPONENT_SIGN;
            }
            if (this.equals(EXPONENT_SYMBOL)) {
                return EXPONENT_SYMBOL;
            }
            if (this.equals(CURRENCY)) {
                return CURRENCY;
            }
            if (this.equals(DECIMAL_SEPARATOR)) {
                return DECIMAL_SEPARATOR;
            }
            if (this.equals(GROUPING_SEPARATOR)) {
                return GROUPING_SEPARATOR;
            }
            if (this.equals(PERCENT)) {
                return PERCENT;
            }
            if (this.equals(PERMILLE)) {
                return PERMILLE;
            }
            if (this.equals(SIGN)) {
                return SIGN;
            }
            // text.02=Unknown attribute
            throw new InvalidObjectException(Messages.getString("text.02")); //$NON-NLS-1$
        }
    }

}
