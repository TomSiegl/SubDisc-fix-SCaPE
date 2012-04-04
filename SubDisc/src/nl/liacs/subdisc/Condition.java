package nl.liacs.subdisc;

public class Condition implements Comparable<Condition>
{
	// Operator Constants
	public static final int ELEMENT_OF		= 0;
	public static final int DOES_NOT_EQUAL		= 1;
	public static final int EQUALS			= 2;
	public static final int LESS_THAN_OR_EQUAL	= 3;
	public static final int GREATER_THAN_OR_EQUAL	= 4;
	public static final int BETWEEN = 5;
	public static final int NOT_AN_OPERATOR		= 99;

	// Binary Operator Constants
	public static final int FIRST_BINARY_OPERATOR	= EQUALS;
	public static final int LAST_BINARY_OPERATOR	= EQUALS;

	// Nominal Operator  Constants
	//MiMa swap these two definitions to get set-valued behaviour
//	public static final int FIRST_NOMINAL_OPERATOR	= ELEMENT_OF;
	public static final int FIRST_NOMINAL_OPERATOR	= DOES_NOT_EQUAL;
	public static final int LAST_NOMINAL_OPERATOR	= EQUALS;

	// Numeric Operator  Constants
	//this allows =, <= and >=
	public static final int FIRST_NUMERIC_OPERATOR	= EQUALS;
	//MiMa swap these two definitions to get set-valued behaviour
	public static final int LAST_NUMERIC_OPERATOR	= BETWEEN;
//	public static final int LAST_NUMERIC_OPERATOR	= GREATER_THAN_OR_EQUAL;

	private final Column itsColumn;
	private final int itsOperator;

	private String itsNominalValue = null;		// ColumnType = NOMINAL
	private ValueSet itsNominalValueSet = null;		// ColumnType = NOMINAL
	private float itsNumericValue = Float.NaN;	// ColumnType = NUMERIC
	private Interval itsInterval = null;		// ColumnType = NUMERIC
	private boolean itsBinaryValue = false;		// ColumnType = BINARY

	/**
	 * Default initialisation values for {@Column Column} of
	 * {@link AttributeType Attribute}:<br>
	 * NOMINAL = <code>null</code>,<br>
	 * NUMERIC = Float.NaN,<br>
	 * BINARY = <code>false</code>.
	 *
	 * @param theColumn
	 */
	public Condition(Column theColumn)
	{
		// TODO null check
		itsColumn = theColumn;

		switch (itsColumn.getType())
		{
			case NOMINAL : itsOperator = FIRST_NOMINAL_OPERATOR; return;
			case NUMERIC : itsOperator = FIRST_NUMERIC_OPERATOR; return;
			case ORDINAL : itsOperator = FIRST_NUMERIC_OPERATOR; return;
			case BINARY : itsOperator = FIRST_BINARY_OPERATOR; return;
			default :
			{
				itsOperator = FIRST_NOMINAL_OPERATOR;
				logTypeError("<init>");
				return;
			}
		}
	}

	/**
	 * Default initialisation values for {@Column Column} of
	 * {@link AttributeType Attribute}:<br>
	 * NOMINAL = <code>null</code>,<br>
	 * NUMERIC = Float.NaN,<br>
	 * BINARY = <code>false</code>.
	 *
	 * @param theColumn
	 */
	public Condition(Column theColumn, int theOperator)
	{
		// TODO null check, operator valid for ColumnType
		itsColumn = theColumn;
		itsOperator = theOperator;
	}

	// obviously does not deep-copy itsColumn
	// itsOperator is primitive type, no need for deep-copy
	// itsValue new String not really needed, as none of current code ever
	// changes it, beside it can be overridden through setValue anyway.
	public Condition copy()
	{
		Condition aCopy = new Condition(itsColumn, itsOperator);
		// new for deep-copy? not strictly needed for code
		if (itsNominalValue != null)
			aCopy.itsNominalValue = new String(itsNominalValue);
		aCopy.itsNominalValueSet = this.itsNominalValueSet; //shallow copy!
		aCopy.itsNumericValue = this.itsNumericValue;
		aCopy.itsInterval = this.itsInterval; //shallow copy!
		aCopy.itsBinaryValue = this.itsBinaryValue;
		return aCopy;
	}

	public Column getColumn() { return itsColumn; }

	public int getOperator() { return itsOperator; }

	private String getValue()
	{
		switch (itsColumn.getType())
		{
			case NOMINAL :
				if (itsNominalValue != null) //single value?
					return itsNominalValue;
				else if (itsNominalValueSet != null) //value set?
					return itsNominalValueSet.toString();
				else
					return null;

			case NUMERIC :
				if (!Float.isNaN(itsNumericValue)) //single value?
					return Float.toString(itsNumericValue);
				else if (itsInterval != null) //interval?
					return itsInterval.toString();
				else
					return null;

			case ORDINAL : return Float.toString(itsNumericValue);
			case BINARY : return itsBinaryValue ? "1" : "0";
			default : logTypeError("getValue"); return "";
		}
	}

	/**
	 * Set the value for this Condition, use:<br>
	 * Floats.toString(theFloatValue) for a <code>float</code>,<br>
	 * "0" or "1" for <code>false</code> and <code>true</code> respectively.
	 */
	/*
	 * Setting the value using a (parsed) String is still sub-optimal, but
	 * unlikely to be a performance drawback. It is done only once per
	 * condition, contrary to subgroup.size()-calls to evaluate().
	 *
	 * Method is called by:
	 * Refinement getRefinedSubgroup
	 * SubgroupDiscovery single nominal constructor
	 * Validation getRandomConditionList randomConditions randomSubgroups
	 */
	public void setValue(String theValue)
	{
		switch (itsColumn.getType())
		{
			case NOMINAL : itsNominalValue = theValue; return;
			case NUMERIC :
			case ORDINAL :
			{
				try { itsNumericValue = Float.parseFloat(theValue); }
				catch (NumberFormatException e) {} // remains NaN
				return;
			}
			case BINARY :
			{
				itsBinaryValue = theValue.equals("1");
				return;
			}
			default : logTypeError("setValue"); return;
		}
	}

	/**
	 * Set the value for this Condition, specifically for nominal value sets
	 */
	public void setValue(ValueSet theValue)	{ itsNominalValueSet = theValue; }

	/**
	 * Set the value for this Condition, specifically for numeric intervals
	 */
	public void setValue(Interval theValue)	{ itsInterval = theValue; }

	public boolean checksNotEquals() { return itsOperator == DOES_NOT_EQUAL; }

	public boolean hasNextOperator()
	{
		if (itsOperator == LAST_BINARY_OPERATOR && itsColumn.isBinaryType())
			return false;
		if (itsOperator == LAST_NOMINAL_OPERATOR && itsColumn.isNominalType())
			return false;
		if (itsOperator == LAST_NUMERIC_OPERATOR && itsColumn.isNumericType())
			return false;
		return true;
	}

	public int getNextOperator()
	{
		return hasNextOperator() ? itsOperator+1 : NOT_AN_OPERATOR;
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#NOMINAL AttributeType.NOMINAL}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition, and {@link String#equals(Object) String.equals()}.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(String theValue)
	{
		switch(itsOperator)
		{
			case ELEMENT_OF :
				return itsNominalValueSet.contains(theValue);
			case DOES_NOT_EQUAL :
				return !theValue.equals(itsNominalValue);
			case EQUALS :
				return theValue.equals(itsNominalValue);
			case LESS_THAN_OR_EQUAL :
			case GREATER_THAN_OR_EQUAL :
			{
				logError("nominal");
				return false;
			}
			default : return false;
		}
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#NUMERIC AttributeType.NUMERIC}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(Float theValue)
	{
		switch(itsOperator)
		{
			case DOES_NOT_EQUAL :
			{
				logError("numeric");
				return false;
			}
			case EQUALS :
				return theValue == itsNumericValue;
			case LESS_THAN_OR_EQUAL :
				return theValue <= itsNumericValue;
			case GREATER_THAN_OR_EQUAL :
				return theValue >= itsNumericValue;
			case BETWEEN:
				return itsInterval.between(theValue);
			default : return false;
		}
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#BINARY AttributeType.BINARY}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(boolean theValue)
	{
		if (itsOperator != EQUALS)
			logError("binary");
		return itsBinaryValue == theValue;
	}

	private void logError(String theColumnType)
	{
		Log.error(String.format("incorrect operator for %s column",
					theColumnType));
	}

	private void logTypeError(String theMethod)
	{
		Log.logCommandLine(String.format("%s.%s(): unknown AttributeType '%s'. Returning '%s'.",
							getClass().getSimpleName(),
							theMethod,
							itsColumn.getType(),
							getOperatorString()));
	}

	private String getOperatorString()
	{
		switch(itsOperator)
		{
			case ELEMENT_OF		: return "in";
			case DOES_NOT_EQUAL		: return "!=";
			case EQUALS			: return "=";
			case LESS_THAN_OR_EQUAL		: return "<=";
			case GREATER_THAN_OR_EQUAL	: return ">=";
			case BETWEEN	: return "in";
			default : return "";
		}
	}

	// never used atm
	private String toCleanString()
	{
		String aName = itsColumn.hasShort() ? itsColumn.getShort() :
							itsColumn.getName();

		if (itsColumn.isNumericType())
			return String.format("%s %s %s",
						aName,
						getOperatorString(),
						getValue());
		else
			return String.format("%s %s '%s'",
						aName,
						getOperatorString(),
						getValue());
	}

	// used by ConditionList.toString()
	@Override
	public String toString()
	{
		return String.format("%s %s '%s'",
					itsColumn.getName(),
					getOperatorString(),
					getValue());
	}

	/*
	 * NOTE
	 * Never override equals() without also overriding hashCode().
	 * Some (Collection) classes use equals to determine equality, others
	 * use hashCode() (eg. java.lang.HashMap).
	 * Failing to override both methods will result in strange behaviour.
	 *
 	 * NOTE
	 * Map interface expects compareTo and equals to be consistent.
	 *
	 * Used by ConditionList.findCondition().
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

/*	@Override
	public boolean equals(Object theObject)
	{
		if (theObject == null || this.getClass() != theObject.getClass())
			return false;
		Condition aCondition = (Condition) theObject;
		if (itsColumn == aCondition.getColumn() &&
			itsOperator == aCondition.getOperator() &&
			itsValue.equals(aCondition.getValue()))
			return true;
		return false;
	}
*/
	// throws NullPointerException if theCondition is null.
	@Override
	public int compareTo(Condition theCondition)
	{
		if (this == theCondition)
			return 0;
		else if (this.itsColumn.getIndex() < theCondition.itsColumn.getIndex())
			return -1;
		else if (this.itsColumn.getIndex() > theCondition.itsColumn.getIndex())
			return 1;
		// same column, check operator
		else if (this.itsOperator < theCondition.itsOperator)
			return -1;
		else if (this.itsOperator > theCondition.itsOperator)
			return 1;
		// same column, same operator, check on value
		/*
		else if (this.getColumn().isNumericType())
			return (Float.valueOf(this.getValue()).compareTo(Float.valueOf(theCondition.getValue())));
		else
		{
			// String.compareTo() does not strictly return -1, 0, 1
			int aCompare = this.getValue().compareTo(theCondition.getValue());
			return (aCompare < 0 ? -1 : aCompare > 0 ? 1 : 0);
		}
		*/
		switch (itsColumn.getType())
		{
			case NOMINAL :
			{
				if (itsNominalValue != null) //single value
				{
					// String.compareTo() does not strictly return -1, 0, 1
					int aCompare = itsNominalValue.compareTo(theCondition.itsNominalValue);
					return (aCompare < 0 ? -1 : aCompare > 0 ? 1 : 0);
				}
				else
				{
					//TODO how to compare two sets of values?
					return Float.compare(itsNominalValueSet.size(), theCondition.itsNominalValueSet.size());
				}
			}
			case NUMERIC :
			case ORDINAL :
			{
				return Float.compare(itsNumericValue, theCondition.itsNumericValue);
			}
			case BINARY :
			{
				if (!itsBinaryValue)
					return theCondition.itsBinaryValue ? -1 : 0;
				else
					return theCondition.itsBinaryValue ? 0 : 1;
			}
			// should never happen
			default :
			{
				logTypeError("compareTo");
				return 0;
			}
		}
	}
}

