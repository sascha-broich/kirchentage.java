package de.sab.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import sun.util.resources.LocaleData;

public class FoeDecimalFormat extends DecimalFormat
{
	private static final long	serialVersionUID	= 1L;
	private DecimalFormatSymbols mySymbols = null;
	// LIU new DecimalFormatSymbols();
	private static final int STATUS_INFINITE = 0;
	private static final int STATUS_POSITIVE = 1;
	private static final int STATUS_LENGTH = 2;
	private transient DigitList digitList = new DigitList();
	private int multiplier = 1;

	private static final char PATTERN_ZERO_DIGIT = '0';
	private static final char PATTERN_GROUPING_SEPARATOR = ',';
	private static final char PATTERN_DECIMAL_SEPARATOR = '.';
	private static final char PATTERN_PER_MILLE = '\u2030';
	private static final char PATTERN_PERCENT = '%';
	private static final char PATTERN_DIGIT = '#';
	private static final char PATTERN_SEPARATOR = ';';
	private static final char PATTERN_EXPONENT = 'E';
	private static final char PATTERN_MINUS = '-';

	private boolean decimalSeparatorAlwaysShown = false;
	private transient boolean isCurrencyFormat = false;

	private boolean useExponentialNotation;
	// Newly persistent in the Java 2 platform

	private static final char CURRENCY_SIGN = '\u00A4';

	private static final char QUOTE = '\'';

	private static FieldPosition[] EmptyFieldPositionArray =
		new FieldPosition[0];

	static final int DOUBLE_INTEGER_DIGITS = 309;
	static final int DOUBLE_FRACTION_DIGITS = 340;
	private static Hashtable<Locale,String> cachedLocaleData = new Hashtable<Locale,String>(3);
	private byte minExponentDigits; // Newly persistent in the Java 2 platform
	private String positivePrefix = "";
	private String positiveSuffix = "";
	private String negativePrefix = "-";
	private String negativeSuffix = "";
	private String posPrefixPattern;
	private String posSuffixPattern;
	private String negPrefixPattern;
	private String negSuffixPattern;
	private byte groupingSize = 3; // invariant, > 0 if useThousands
	private transient FieldPosition[] positivePrefixFieldPositions;
	private transient FieldPosition[] positiveSuffixFieldPositions;
	private transient FieldPosition[] negativePrefixFieldPositions;
	private transient FieldPosition[] negativeSuffixFieldPositions;

	/**
	 * 
	 */
	public FoeDecimalFormat()
	{
		super();
		Locale def = Locale.getDefault();
		// try to get the pattern from the cache
		String pattern = cachedLocaleData.get(def);
		if (pattern == null)
		{ /* cache miss */
			// Get the pattern for the default locale.
			ResourceBundle rb = new LocaleData(sun.util.locale.provider.LocaleProviderAdapter.Type.JRE).getNumberFormatData(def);
			String[] all = rb.getStringArray("NumberPatterns");
			pattern = all[0];
			/* update cache */
			cachedLocaleData.put(def, pattern);
		}

		// Always applyPattern after the symbols are set
		this.mySymbols = new DecimalFormatSymbols(def);
		applyPattern(pattern, false);
	}

	/**
	 * @param pattern
	 */
	public FoeDecimalFormat(String pattern)
	{
		super(pattern);
		// Always applyPattern after the symbols are set
		this.mySymbols = new DecimalFormatSymbols(Locale.getDefault());
		applyPattern(pattern, false);
	}

	/**
	 * @param pattern
	 * @param symbols
	 */
	public FoeDecimalFormat(String pattern, DecimalFormatSymbols symbols)
	{
		super(pattern, symbols);
		// Always applyPattern after the symbols are set
		this.mySymbols = (DecimalFormatSymbols)symbols.clone();
		applyPattern(pattern, false);
	}

	public Number parse(String source) throws ParseException 
	{
		ParsePosition parsePosition = new ParsePosition(0);
		Number result = parse(source, parsePosition);
		if (parsePosition.getIndex()== 0 || parsePosition.getIndex()<source.length()) 
		{
			throw new ParseException("Unparseable number: \"" + source + "\"",
				parsePosition.getErrorIndex());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.text.NumberFormat#parse(java.lang.String, java.text.ParsePosition)
	 */
	public Number parse(String text, ParsePosition pos)
	{
		// special case NaN
		if (text
			.regionMatches(
				pos.getIndex(),
				mySymbols.getNaN(),
				0,
				mySymbols.getNaN().length()))
		{
			pos.setIndex(pos.getIndex() + mySymbols.getNaN().length());
			return new Double(Double.NaN);
		}

		boolean[] status = new boolean[STATUS_LENGTH];

		if (!subparse(text, pos, digitList, false, status))
			return null;
		// There are forbidden symbols after the number
		if(pos.getIndex()<text.length()) return null;

		double doubleResult = 0.0;
		long longResult = 0;
		boolean gotDouble = true;

		// Finally, have DigitList parse the digits into a value.
		if (status[STATUS_INFINITE])
		{
			doubleResult = Double.POSITIVE_INFINITY;
		}
		else if (
			digitList.fitsIntoLong(
				status[STATUS_POSITIVE],
				isParseIntegerOnly()))
		{
			gotDouble = false;
			longResult = digitList.getLong();
		}
		else
			doubleResult = digitList.getDouble();

		// Divide by multiplier. We have to be careful here not to do unneeded
		// conversions between double and long.
		if (multiplier != 1)
		{
			if (gotDouble)
				doubleResult /= multiplier;
			else
			{
				// Avoid converting to double if we can
				if (longResult % multiplier == 0)
				{
					longResult /= multiplier;
				}
				else
				{
					doubleResult = ((double)longResult) / multiplier;
					if (doubleResult < 0)
						doubleResult = -doubleResult;
					gotDouble = true;
				}
			}
		}

		if (!status[STATUS_POSITIVE])
		{
			doubleResult = -doubleResult;
			// If longResult was Long.MIN_VALUE or a divisor of it (if
			// multiplier != 1) then don't negate it.
			if (longResult > 0)
			{
				longResult = -longResult;
			}
		}

		// At this point, if we divided the result by the multiplier, the result may
		// fit into a long.  We check for this case and return a long if possible.
		// We must do this AFTER applying the negative (if appropriate) in order to
		// handle the case of LONG_MIN; otherwise, if we do this with a positive value
		// -LONG_MIN, the double is > 0, but the long is < 0.  This is a C++-specific
		// situation.  We also must retain a double in the case of -0.0, which will
		// compare as == to a long 0 cast to a double (bug 4162852).
		if (multiplier != 1 && gotDouble)
		{
			longResult = (long)doubleResult;
			gotDouble =
				(doubleResult != longResult)
					|| (doubleResult == 0.0
						&& !status[STATUS_POSITIVE]
						&& !isParseIntegerOnly());
		}

		return gotDouble
			? (Number)new Double(doubleResult)
			: (Number)new Long(longResult);
	}

	/**
	  * Does the real work of applying a pattern.
	  */
	private void applyPattern(String pattern, boolean localized)
	{
		char zeroDigit = PATTERN_ZERO_DIGIT;
		char groupingSeparator = PATTERN_GROUPING_SEPARATOR;
		char decimalSeparator = PATTERN_DECIMAL_SEPARATOR;
		char percent = PATTERN_PERCENT;
		char perMill = PATTERN_PER_MILLE;
		char digit = PATTERN_DIGIT;
		char separator = PATTERN_SEPARATOR;
		char exponent = PATTERN_EXPONENT;
		char minus = PATTERN_MINUS;
		if (localized)
		{
			zeroDigit = mySymbols.getZeroDigit();
			groupingSeparator = mySymbols.getGroupingSeparator();
			decimalSeparator = mySymbols.getDecimalSeparator();
			percent = mySymbols.getPercent();
			perMill = mySymbols.getPerMill();
			digit = mySymbols.getDigit();
			separator = mySymbols.getPatternSeparator();
			exponent = 'E';
			minus = mySymbols.getMinusSign();
		}
		boolean gotNegative = false;

		decimalSeparatorAlwaysShown = false;
		isCurrencyFormat = false;
		useExponentialNotation = false;

		// Two variables are used to record the subrange of the pattern
		// occupied by phase 1.  This is used during the processing of the
		// second pattern (the one representing negative numbers) to ensure
		// that no deviation exists in phase 1 between the two patterns.
		//		 int phaseOneStart = 0;
		int phaseOneLength = 0;
		/** Back-out comment : HShih
		 * boolean phaseTwo = false;
		 */

		int start = 0;
		for (int j = 1; j >= 0 && start < pattern.length(); --j)
		{
			boolean inQuote = false;
			StringBuffer prefix = new StringBuffer();
			StringBuffer suffix = new StringBuffer();
			int decimalPos = -1;
			int multiplier1 = 1;
			int digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0;
			byte groupingCount = -1;

			// The phase ranges from 0 to 2.  Phase 0 is the prefix.  Phase 1 is
			// the section of the pattern with digits, decimal separator,
			// grouping characters.  Phase 2 is the suffix.  In phases 0 and 2,
			// percent, permille, and currency symbols are recognized and
			// translated.  The separation of the characters into phases is
			// strictly enforced; if phase 1 characters are to appear in the
			// suffix, for example, they must be quoted.
			int phase = 0;

			// The affix is either the prefix or the suffix.
			StringBuffer affix = prefix;

			for (int pos = start; pos < pattern.length(); ++pos)
			{
				char ch = pattern.charAt(pos);
				switch (phase)
				{
					case 0 :
					case 2 :
						// Process the prefix / suffix characters
						if (inQuote)
						{
							// A quote within quotes indicates either the closing
							// quote or two quotes, which is a quote literal.  That is,
							// we have the second quote in 'do' or 'don''t'.
							if (ch == QUOTE)
							{
								if ((pos + 1) < pattern.length()
									&& pattern.charAt(pos + 1) == QUOTE)
								{
									++pos;
									affix.append("''"); // 'don''t'
								}
								else
								{
									inQuote = false; // 'do'
								}
								continue;
							}
						}
						else
						{
							// Process unquoted characters seen in prefix or suffix
							// phase.
							if (ch == digit
								|| ch == zeroDigit
								|| ch == groupingSeparator
								|| ch == decimalSeparator)
							{
								// Any of these characters implicitly begins the next
								// phase.  If we are in phase 2, there is no next phase,
								// so these characters are illegal.
								/**
								 * 1.2 Back-out comment : HShih
								 * Can't throw exception here.
								 * if (phase == 2)
								 *    throw new IllegalArgumentException("Unquoted special character '" +
								 *                   ch + "' in pattern \"" +
								 *                   pattern + '"');
								 */
								phase = 1;
								//						 if (j == 1) phaseOneStart = pos;
								--pos; // Reprocess this character
								continue;
							}
							else if (ch == CURRENCY_SIGN)
							{
								// Use lookahead to determine if the currency sign is
								// doubled or not.
								boolean doubled =
									(pos + 1) < pattern.length()
										&& pattern.charAt(pos + 1)
											== CURRENCY_SIGN;
								if (doubled)
									++pos; // Skip over the doubled character
								isCurrencyFormat = true;
								affix.append(
									doubled ? "'\u00A4\u00A4" : "'\u00A4");
								continue;
							}
							else if (ch == QUOTE)
							{
								// A quote outside quotes indicates either the opening
								// quote or two quotes, which is a quote literal.  That is,
								// we have the first quote in 'do' or o''clock.
								if (ch == QUOTE)
								{
									if ((pos + 1) < pattern.length()
										&& pattern.charAt(pos + 1) == QUOTE)
									{
										++pos;
										affix.append("''"); // o''clock
									}
									else
									{
										inQuote = true; // 'do'
									}
									continue;
								}
							}
							else if (ch == separator)
							{
								// Don't allow separators before we see digit characters of phase
								// 1, and don't allow separators in the second pattern (j == 0).
								if (phase == 0 || j == 0)
									throw new IllegalArgumentException(
										"Unquoted special character '"
											+ ch
											+ "' in pattern \""
											+ pattern
											+ '"');
								start = pos + 1;
								pos = pattern.length();
								continue;
							}

							// Next handle characters which are appended directly.
							else if (ch == percent)
							{
								if (multiplier1 != 1)
									throw new IllegalArgumentException(
										"Too many percent/permille characters in pattern \""
											+ pattern
											+ '"');
								multiplier1 = 100;
								affix.append("'%");
								continue;
							}
							else if (ch == perMill)
							{
								if (multiplier1 != 1)
									throw new IllegalArgumentException(
										"Too many percent/permille characters in pattern \""
											+ pattern
											+ '"');
								multiplier1 = 1000;
								affix.append("'\u2030");
								continue;
							}
							else if (ch == minus)
							{
								affix.append("'-");
								continue;
							}
						}
						// Note that if we are within quotes, or if this is an unquoted,
						// non-special character, then we usually fall through to here.
						affix.append(new char[]{ch});
						break;
					case 1 :
						// Phase one must be identical in the two sub-patterns.  We
						// enforce this by doing a direct comparison.  While
						// processing the first sub-pattern, we just record its
						// length.  While processing the second, we compare
						// characters.
						if (j == 1)
							++phaseOneLength;
						else
						{
							/**
							 * 1.2 Back-out comment : HShih
							 * if (ch != pattern.charAt(phaseOneStart++))
							 *    throw new IllegalArgumentException("Subpattern mismatch in \"" +
							 *                       pattern + '"');
							 * phaseTwo = true;
							 */
							if (--phaseOneLength == 0)
							{
								phase = 2;
								affix = suffix;
							}
							continue;
						}

						// Process the digits, decimal, and grouping characters.  We
						// record five pieces of information.  We expect the digits
						// to occur in the pattern ####0000.####, and we record the
						// number of left digits, zero (central) digits, and right
						// digits.  The position of the last grouping character is
						// recorded (should be somewhere within the first two blocks
						// of characters), as is the position of the decimal point,
						// if any (should be in the zero digits).  If there is no
						// decimal point, then there should be no right digits.
						if (ch == digit)
						{
							if (zeroDigitCount > 0)
								++digitRightCount;
							else
								++digitLeftCount;
							if (groupingCount >= 0 && decimalPos < 0)
								++groupingCount;
						}
						else if (ch == zeroDigit)
						{
							if (digitRightCount > 0)
								throw new IllegalArgumentException(
									"Unexpected '0' in pattern \""
										+ pattern
										+ '"');
							++zeroDigitCount;
							if (groupingCount >= 0 && decimalPos < 0)
								++groupingCount;
						}
						else if (ch == groupingSeparator)
						{
							groupingCount = 0;
						}
						else if (ch == decimalSeparator)
						{
							if (decimalPos >= 0)
								throw new IllegalArgumentException(
									"Multiple decimal separators in pattern \""
										+ pattern
										+ '"');
							decimalPos =
								digitLeftCount
									+ zeroDigitCount
									+ digitRightCount;
						}
						else if (ch == exponent)
						{
							if (useExponentialNotation)
								throw new IllegalArgumentException(
									"Multiple exponential "
										+ "symbols in pattern \""
										+ pattern
										+ '"');
							useExponentialNotation = true;
							minExponentDigits = 0;

							// Use lookahead to parse out the exponential part of the
							// pattern, then jump into phase 2.
							while (++pos < pattern.length()
								&& pattern.charAt(pos) == zeroDigit)
							{
								++minExponentDigits;
								++phaseOneLength;
							}

							if ((digitLeftCount + zeroDigitCount) < 1
								|| minExponentDigits < 1)
								throw new IllegalArgumentException(
									"Malformed exponential "
										+ "pattern \""
										+ pattern
										+ '"');

							// Transition to phase 2
							phase = 2;
							affix = suffix;
							--pos;
							continue;
						}
						else
						{
							phase = 2;
							affix = suffix;
							--pos;
							--phaseOneLength;
							continue;
						}
						break;
				}
			}
			/**
			 * 1.2 Back-out comment : HShih
			 * if (phaseTwo && phaseOneLength > 0)
			 *      throw new IllegalArgumentException("Subpattern mismatch in \"" +
			 *                                   pattern + '"');
			 */
			// Handle patterns with no '0' pattern character.  These patterns
			// are legal, but must be interpreted.  "##.###" -> "#0.###".
			// ".###" -> ".0##".
			/* We allow patterns of the form "####" to produce a zeroDigitCount of
			 * zero (got that?); although this seems like it might make it possible
			 * for format() to produce empty strings, format() checks for this
			 * condition and outputs a zero digit in this situation.  Having a
			 * zeroDigitCount of zero yields a minimum integer digits of zero, which
			 * allows proper round-trip patterns.  That is, we don't want "#" to
			 * become "#0" when toPattern() is called (even though that's what it
			 * really is, semantically).  */
			if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0)
			{
				// Handle "###.###" and "###." and ".###"
				int n = decimalPos;
				if (n == 0)
					++n; // Handle ".###"
				digitRightCount = digitLeftCount - n;
				digitLeftCount = n - 1;
				zeroDigitCount = 1;
			}

			// Do syntax checking on the digits.
			if ((decimalPos < 0 && digitRightCount > 0)
				|| (decimalPos >= 0
					&& (decimalPos < digitLeftCount
						|| decimalPos > (digitLeftCount + zeroDigitCount)))
				|| groupingCount == 0
				|| inQuote)
				throw new IllegalArgumentException(
					"Malformed pattern \"" + pattern + '"');

			if (j == 1)
			{
				posPrefixPattern = prefix.toString();
				posSuffixPattern = suffix.toString();
				negPrefixPattern = posPrefixPattern; // assume these for now
				negSuffixPattern = posSuffixPattern;
				int digitTotalCount =
					digitLeftCount + zeroDigitCount + digitRightCount;
				/* The effectiveDecimalPos is the position the decimal is at or
				 * would be at if there is no decimal.  Note that if decimalPos<0,
				 * then digitTotalCount == digitLeftCount + zeroDigitCount.  */
				int effectiveDecimalPos =
					decimalPos >= 0 ? decimalPos : digitTotalCount;
				setMinimumIntegerDigits(effectiveDecimalPos - digitLeftCount);
				setMaximumIntegerDigits(
					useExponentialNotation
						? digitLeftCount + getMinimumIntegerDigits()
						: DOUBLE_INTEGER_DIGITS);
				setMaximumFractionDigits(
					decimalPos >= 0 ? (digitTotalCount - decimalPos) : 0);
				setMinimumFractionDigits(
					decimalPos >= 0
						? (digitLeftCount + zeroDigitCount - decimalPos)
						: 0);
				setGroupingUsed(groupingCount > 0);
				this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
				this.multiplier = multiplier1;
				setDecimalSeparatorAlwaysShown(
					decimalPos == 0 || decimalPos == digitTotalCount);
			}
			else
			{
				negPrefixPattern = prefix.toString();
				negSuffixPattern = suffix.toString();
				gotNegative = true;
			}
		}

		if (pattern.length() == 0)
		{
			posPrefixPattern = posSuffixPattern = "";
			setMinimumIntegerDigits(0);
			setMaximumIntegerDigits(DOUBLE_INTEGER_DIGITS);
			setMinimumFractionDigits(0);
			setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
		}

		// If there was no negative pattern, or if the negative pattern is identical
		// to the positive pattern, then prepend the minus sign to the positive
		// pattern to form the negative pattern.
		if (!gotNegative
			|| (negPrefixPattern.equals(posPrefixPattern)
				&& negSuffixPattern.equals(posSuffixPattern)))
		{
			negSuffixPattern = posSuffixPattern;
			negPrefixPattern = "'-" + posPrefixPattern;
		}

		expandAffixes();
	}

	/**
	 * Parse the given text into a number.  The text is parsed beginning at
	 * parsePosition, until an unparseable character is seen.
	 * @param text The string to parse.
	 * @param parsePosition The position at which to being parsing.  Upon
	 * return, the first unparseable character.
	 * @param digits The DigitList to set to the parsed value.
	 * @param isExponent If true, parse an exponent.  This means no
	 * infinite values and integer only.
	 * @param status Upon return contains boolean status flags indicating
	 * whether the value was infinite and whether it was positive.
	 */
	private final boolean subparse(
		String text,
		ParsePosition parsePosition,
		DigitList digits,
		boolean isExponent,
		boolean status[])
	{
		int position = parsePosition.getIndex();
		int oldStart = parsePosition.getIndex();
		int backup;

		// check for positivePrefix; take longest
		boolean gotPositive =
			text.regionMatches(
				position,
				positivePrefix,
				0,
				positivePrefix.length());
		boolean gotNegative =
			text.regionMatches(
				position,
				negativePrefix,
				0,
				negativePrefix.length());
		if (gotPositive && gotNegative)
		{
			if (positivePrefix.length() > negativePrefix.length())
				gotNegative = false;
			else if (positivePrefix.length() < negativePrefix.length())
				gotPositive = false;
		}
		if (gotPositive)
		{
			position += positivePrefix.length();
		}
		else if (gotNegative)
		{
			position += negativePrefix.length();
		}
		else
		{
			parsePosition.setErrorIndex(position);
			return false;
		}
		// process digits or Inf, find decimal position
		status[STATUS_INFINITE] = false;
		if (!isExponent
			&& text.regionMatches(
				position,
				mySymbols.getInfinity(),
				0,
				mySymbols.getInfinity().length()))
		{
			position += mySymbols.getInfinity().length();
			status[STATUS_INFINITE] = true;
		}
		else
		{
			// We now have a string of digits, possibly with grouping symbols,
			// and decimal points.  We want to process these into a DigitList.
			// We don't want to put a bunch of leading zeros into the DigitList
			// though, so we keep track of the location of the decimal point,
			// put only significant digits into the DigitList, and adjust the
			// exponent as needed.

			digits.decimalAt = digits.count = 0;
			char zero = mySymbols.getZeroDigit();
			char decimal =
				isCurrencyFormat
					? mySymbols.getMonetaryDecimalSeparator()
					: mySymbols.getDecimalSeparator();
			char grouping = mySymbols.getGroupingSeparator();
			char exponentChar = 'E';
			boolean sawDecimal = false;
			boolean sawExponent = false;
			boolean sawDigit = false;
			int exponent = 0; // Set to the exponent value, if any

			// We have to track digitCount ourselves, because digits.count will
			// pin when the maximum allowable digits is reached.
			int digitCount = 0;

			backup = -1;
			for (; position < text.length(); ++position)
			{
				char ch = text.charAt(position);

				/* We recognize all digit ranges, not only the Latin digit range
				 * '0'..'9'.  We do so by using the Character.digit() method,
				 * which converts a valid Unicode digit to the range 0..9.
				 *
				 * The character 'ch' may be a digit.  If so, place its value
				 * from 0 to 9 in 'digit'.  First try using the locale digit,
				 * which may or MAY NOT be a standard Unicode digit range.  If
				 * this fails, try using the standard Unicode digit ranges by
				 * calling Character.digit().  If this also fails, digit will
				 * have a value outside the range 0..9.
				 */
				int digit = ch - zero;
				if (digit < 0 || digit > 9)
					digit = Character.digit(ch, 10);

				if (digit == 0)
				{
					// Cancel out backup setting (see grouping handler below)
					backup = -1; // Do this BEFORE continue statement below!!!
					sawDigit = true;

					// Handle leading zeros
					if (digits.count == 0)
					{
						// Ignore leading zeros in integer part of number.
						if (!sawDecimal)
							continue;

						// If we have seen the decimal, but no significant digits yet,
						// then we account for leading zeros by decrementing the
						// digits.decimalAt into negative values.
						--digits.decimalAt;
					}
					else
					{
						++digitCount;
						digits.append((char) (digit + '0'));
					}
				}
				else if (
					digit > 0 && digit <= 9) // [sic] digit==0 handled above
				{
					sawDigit = true;
					++digitCount;
					digits.append((char) (digit + '0'));

					// Cancel out backup setting (see grouping handler below)
					backup = -1;
				}
				else if (!isExponent && ch == decimal)
				{
					// If we're only parsing integers, or if we ALREADY saw the
					// decimal, then don't parse this one.
					if (isParseIntegerOnly() || sawDecimal)
						break;
					digits.decimalAt = digitCount; // Not digits.count!
					sawDecimal = true;
				}
				else if (!isExponent && ch == grouping && isGroupingUsed())
				{
					if (sawDecimal)
					{
						break;
					}
					// Ignore grouping characters, if we are using them, but require
					// that they be followed by a digit.  Otherwise we backup and
					// reprocess them.
					backup = position;
				}
				else if (!isExponent && Character.toUpperCase(ch) == exponentChar && !sawExponent)
				{
					// Process the exponent by recursively calling this method.
					ParsePosition pos = new ParsePosition(position + 1);
					boolean[] stat = new boolean[STATUS_LENGTH];
					DigitList exponentDigits = new DigitList();

					if (subparse(text, pos, exponentDigits, true, stat)
						&& exponentDigits.fitsIntoLong(
							stat[STATUS_POSITIVE],
							true))
					{
						position = pos.getIndex(); // Advance past the exponent
						exponent = (int)exponentDigits.getLong();
						if (!stat[STATUS_POSITIVE])
							exponent = -exponent;
						sawExponent = true;
					}
					break; // Whether we fail or succeed, we exit this loop
				}
				else
					break;
			}

			if (backup != -1)
				position = backup;

			// If there was no decimal point we have an integer
			if (!sawDecimal)
				digits.decimalAt = digitCount; // Not digits.count!

			// Adjust for exponent, if any
			digits.decimalAt += exponent;

			// If none of the text string was recognized.  For example, parse
			// "x" with pattern "#0.00" (return index and error index both 0)
			// parse "$" with pattern "$#0.00". (return index 0 and error index
			// 1).
			if (!sawDigit && digitCount == 0)
			{
				parsePosition.setIndex(oldStart);
				parsePosition.setErrorIndex(oldStart);
				return false;
			}
		}

		// check for positiveSuffix
		if (gotPositive)
			gotPositive =
				text.regionMatches(
					position,
					positiveSuffix,
					0,
					positiveSuffix.length());
		if (gotNegative)
			gotNegative =
				text.regionMatches(
					position,
					negativeSuffix,
					0,
					negativeSuffix.length());

		// if both match, take longest
		if (gotPositive && gotNegative)
		{
			if (positiveSuffix.length() > negativeSuffix.length())
				gotNegative = false;
			else if (positiveSuffix.length() < negativeSuffix.length())
				gotPositive = false;
		}

		// fail if neither or both
		if (gotPositive == gotNegative)
		{
			parsePosition.setErrorIndex(position);
			return false;
		}

		parsePosition.setIndex(
			position
				+ (gotPositive
					? positiveSuffix.length()
					: negativeSuffix.length()));
		// mark success!

		status[STATUS_POSITIVE] = gotPositive;
		if (parsePosition.getIndex() == oldStart)
		{
			parsePosition.setErrorIndex(position);
			return false;
		}
		return true;
	}

	/**
	  * Expand the affix pattern strings into the expanded affix strings.  If any
	  * affix pattern string is null, do not expand it.  This method should be
	  * called any time the symbols or the affix patterns change in order to keep
	  * the expanded affix strings up to date.
	  */
	private void expandAffixes()
	{
		// Reuse one StringBuffer for better performance
		StringBuffer buffer = new StringBuffer();
		if (posPrefixPattern != null)
		{
			positivePrefix = expandAffix(posPrefixPattern, buffer);
			positivePrefixFieldPositions = null;
		}
		if (posSuffixPattern != null)
		{
			positiveSuffix = expandAffix(posSuffixPattern, buffer);
			positiveSuffixFieldPositions = null;
		}
		if (negPrefixPattern != null)
		{
			negativePrefix = expandAffix(negPrefixPattern, buffer);
			negativePrefixFieldPositions = null;
		}
		if (negSuffixPattern != null)
		{
			negativeSuffix = expandAffix(negSuffixPattern, buffer);
			negativeSuffixFieldPositions = null;
		}
	}
	private String expandAffix(String pattern, StringBuffer buffer)
	{
		buffer.delete(0,buffer.length());
		for (int i = 0; i < pattern.length();)
		{
			char c = pattern.charAt(i++);
			if (c == QUOTE)
			{
				c = pattern.charAt(i++);
				switch (c)
				{
					case CURRENCY_SIGN :
						if (i < pattern.length()
							&& pattern.charAt(i) == CURRENCY_SIGN)
						{
							++i;
							buffer.append(
								mySymbols.getInternationalCurrencySymbol());
						}
						else
						{
							buffer.append(mySymbols.getCurrencySymbol());
						}
						continue;
					case PATTERN_PERCENT :
						c = mySymbols.getPercent();
						break;
					case PATTERN_PER_MILLE :
						c = mySymbols.getPerMill();
						break;
					case PATTERN_MINUS :
						c = mySymbols.getMinusSign();
						break;
				}
			}
			buffer.append(new char[]{c});
		}
		return buffer.toString();
	}

}