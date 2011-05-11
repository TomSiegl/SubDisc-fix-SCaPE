package nl.liacs.subdisc;

import java.util.*;

/**
 * Functionality related to the statistical validation of subgroups
 */
public class Validation
{
	private SearchParameters itsSearchParameters;
	private TargetConcept itsTargetConcept;
	private QualityMeasure itsQualityMeasure;
	private Table itsTable;

	public Validation(SearchParameters theSearchParameters, Table theTable, QualityMeasure theQualityMeasure)
	{
		itsSearchParameters = theSearchParameters;
		itsTargetConcept = theSearchParameters.getTargetConcept();
		itsTable = theTable;
		itsQualityMeasure = theQualityMeasure;
	}

	public double[] randomSubgroups(int theNrRepetitions)
	{
		double[] aQualities = new double[theNrRepetitions];
		Random aRandom = new Random(System.currentTimeMillis());
		int aNrRows  =itsTable.getNrRows();
		int aMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		int aSubgroupSize;

		switch(itsTargetConcept.getTargetType())
		{
			case SINGLE_NOMINAL :
			{
				Column aTarget = itsTargetConcept.getPrimaryTarget();
				Condition aCondition = new Condition(aTarget, Condition.EQUALS);
				aCondition.setValue(itsTargetConcept.getTargetValue());
				BitSet aBinaryTarget = itsTable.evaluate(aCondition);

				for (int i=0; i<theNrRepetitions; i++)
				{
					do
						aSubgroupSize = (int) (aRandom.nextDouble() * aNrRows);
					while (aSubgroupSize < aMinimumCoverage  || aSubgroupSize==aNrRows);
					Subgroup aSubgroup = itsTable.getRandomSubgroup(aSubgroupSize);

					BitSet aColumnTarget = (BitSet) aBinaryTarget.clone();
					aColumnTarget.and(aSubgroup.getMembers());
					int aCountHeadBody = aColumnTarget.cardinality();
					aQualities[i] = itsQualityMeasure.calculate(aCountHeadBody, aSubgroup.getCoverage());
				}
				break;
			}
			case MULTI_LABEL :
			{
				//base model
				BinaryTable aBaseTable = new BinaryTable(itsTable, itsTargetConcept.getMultiTargets());
				Bayesian aBayesian = new Bayesian(aBaseTable);
				aBayesian.climb();

				for (int i=0; i<theNrRepetitions; i++)
				{
					do
						aSubgroupSize = (int) (aRandom.nextDouble() * aNrRows);
					while (aSubgroupSize < aMinimumCoverage  || aSubgroupSize==aNrRows);
					Subgroup aSubgroup = itsTable.getRandomSubgroup(aSubgroupSize);

					// build model
					BinaryTable aBinaryTable = aBaseTable.selectRows(aSubgroup.getMembers());
					aBayesian = new Bayesian(aBinaryTable);
					aBayesian.climb();
					aSubgroup.setDAG(aBayesian.getDAG()); // store DAG with subgroup for later use

					aQualities[i] = itsQualityMeasure.calculate(aSubgroup);
				}
				break;
			}
			case DOUBLE_REGRESSION :
				//TODO implement
			case DOUBLE_CORRELATION :
			{
//				Column aPrimaryColumn = itsTable.getColumn(itsTargetConcept.getPrimaryTarget());
//				Column aSecondaryColumn = itsTable.getColumn(itsTargetConcept.getSecondaryTarget());
				Column aPrimaryColumn = itsTargetConcept.getPrimaryTarget();
				Column aSecondaryColumn = itsTargetConcept.getSecondaryTarget();
				CorrelationMeasure itsBaseCM =
					new CorrelationMeasure(itsSearchParameters.getQualityMeasure(), aPrimaryColumn, aSecondaryColumn);

				for (int i=0; i<theNrRepetitions; i++)
				{
					do
						aSubgroupSize = (int) (aRandom.nextDouble() * aNrRows);
					while (aSubgroupSize < aMinimumCoverage  || aSubgroupSize==aNrRows);
					Subgroup aSubgroup = itsTable.getRandomSubgroup(aSubgroupSize);

					CorrelationMeasure aCM = new CorrelationMeasure(itsBaseCM);

					for (int j=0; j<aNrRows; j++)
						if (aSubgroup.getMembers().get(j))
							aCM.addObservation(aPrimaryColumn.getFloat(j), aSecondaryColumn.getFloat(j));

					aQualities[i] = aCM.getEvaluationMeasureValue();
				}
				break;
			}
		}
		return aQualities; //return the qualities of the random sample
	}


	/**
	* Generates a set of random descriptions of subgroups, by randomly combining random conditions on
	* attributes in the table. The random descriptions adhere to the search parameters.
	* For each of the subgroups related to the random conditions, the quality is computed.
	* @return the computed qualities.
	*/
	public double[] randomConditions(int theNrRepetitions)
	{
		double[] aQualities = new double[theNrRepetitions];
		Random aRandom = new Random(System.currentTimeMillis());
		int aDepth = itsSearchParameters.getSearchDepth();
		int aMinimumCoverage = itsSearchParameters.getMinimumCoverage();
		int aNrRows = itsTable.getNrRows();

		switch(itsTargetConcept.getTargetType())
		{
			case SINGLE_NOMINAL :
			{
				Column aTarget = itsTargetConcept.getPrimaryTarget();
				Condition aCondition = new Condition(aTarget, Condition.EQUALS);
				aCondition.setValue(itsTargetConcept.getTargetValue());
				BitSet aBinaryTarget = itsTable.evaluate(aCondition);

				for (int i=0; i<theNrRepetitions; i++)
				{
					ConditionList aCL;
					BitSet aMembers;
					do
					{
						aCL = getRandomConditionList(aDepth, aRandom);
						aMembers = itsTable.evaluate(aCL);
					}
					while (aMembers.cardinality() < aMinimumCoverage || aMembers.cardinality()==aNrRows);
//					Log.logCommandLine(aCL.toString());
					Subgroup aSubgroup = new Subgroup(aCL, aMembers, aCL.size());

					BitSet aColumnTarget = (BitSet) aBinaryTarget.clone();
					aColumnTarget.and(aSubgroup.getMembers());
					int aCountHeadBody = aColumnTarget.cardinality();
					aQualities[i] = itsQualityMeasure.calculate(aCountHeadBody, aSubgroup.getCoverage());
				}
				break;
			}
			case MULTI_LABEL :
			{
				// base model
				BinaryTable aBaseTable = new BinaryTable(itsTable, itsTargetConcept.getMultiTargets());
				Bayesian aBayesian = new Bayesian(aBaseTable);
				aBayesian.climb();

				for (int i = 0; i < theNrRepetitions; i++) // random conditions
				{
					ConditionList aCL;
					BitSet aMembers;
					do
					{
						aCL = getRandomConditionList(aDepth, aRandom);
						aMembers = itsTable.evaluate(aCL);
					}
					while (aMembers.cardinality() < aMinimumCoverage || aMembers.cardinality()==aNrRows);
					Log.logCommandLine(aCL.toString());
					Subgroup aSubgroup = new Subgroup(aCL, aMembers, aCL.size());

					// build model
					BinaryTable aBinaryTable = aBaseTable.selectRows(aMembers);
					aBayesian = new Bayesian(aBinaryTable);
					aBayesian.climb();
					aSubgroup.setDAG(aBayesian.getDAG()); // store DAG with subgroup for later use

					aQualities[i] = itsQualityMeasure.calculate(aSubgroup);
					Log.logCommandLine((i + 1) + "," + aSubgroup.getCoverage() + "," + aQualities[i]);
				}

				break;
			}
			case DOUBLE_REGRESSION :
				//TODO implement
			case DOUBLE_CORRELATION :
			{
//				Column aPrimaryColumn = itsTable.getColumn(itsTargetConcept.getPrimaryTarget());
//				Column aSecondaryColumn = itsTable.getColumn(itsTargetConcept.getSecondaryTarget());
				Column aPrimaryColumn = itsTargetConcept.getPrimaryTarget();
				Column aSecondaryColumn = itsTargetConcept.getSecondaryTarget();
				CorrelationMeasure itsBaseCM =
					new CorrelationMeasure(itsSearchParameters.getQualityMeasure(), aPrimaryColumn, aSecondaryColumn);

				for (int i=0; i<theNrRepetitions; i++)
				{
					ConditionList aCL;
					BitSet aMembers;
					do
					{
						aCL = getRandomConditionList(aDepth, aRandom);
						aMembers = itsTable.evaluate(aCL);
					}
					while (aMembers.cardinality() < aMinimumCoverage || aMembers.cardinality()==aNrRows);
					Log.logCommandLine(aCL.toString());
					Subgroup aSubgroup = new Subgroup(aCL, aMembers, aCL.size());

					CorrelationMeasure aCM = new CorrelationMeasure(itsBaseCM);

					for (int j=0; j<aNrRows; j++)
						if (aSubgroup.getMembers().get(j))
							aCM.addObservation(aPrimaryColumn.getFloat(j), aSecondaryColumn.getFloat(j));

					aQualities[i] = aCM.getEvaluationMeasureValue();
				}
				break;
			}
		}
		return aQualities; //return the qualities belonging to this random sample
	}
	
	/* 
	 * KNOWN BUG:
	 * 
	 * Swap randomizes the original Table. When this method is called from the MiningWindow, the swap randomized Columns are restored, 
	 * but when the method is called from the ResultWindow they are not. 
	 * 
	 * "I can't understand what the problem is, I find it hard enough dealing with my own biz."
	 *                                                -- De La Soul, Ring Ring Ring (Ha Ha Hey) 
	 */
	public double[] swapRandomization(int theNrRepetitions)
	{
		// Memorizing the COMMANDLINELOG setting, creating a place for the to be generated qualities
		boolean aCOMMANDLINELOGmem = Log.COMMANDLINELOG;
		double[] aQualities = new double[theNrRepetitions];
		
		// Initializing variables		
		SubgroupDiscovery aSubgroupDiscovery = null;
		int itsPositiveCount = 0;
		float itsTargetAverage = 0;
		Column aPrimaryCopy = null;
		Column aSecondaryCopy = null;
		List<Column> aMultiCopy = new ArrayList<Column>();
		
		// Do some administration to enable running SD, store columns that will soon be swap randomized
		switch(itsTargetConcept.getTargetType())
		{
			case SINGLE_NOMINAL :
			{
				itsPositiveCount = itsTable.countValues(itsTargetConcept.getPrimaryTarget().getIndex(), itsTargetConcept.getTargetValue());
				aPrimaryCopy = itsTargetConcept.getPrimaryTarget().copy();
				break;
			}
			case SINGLE_NUMERIC :
			{
				itsTargetAverage = itsTable.getAverage(itsTargetConcept.getPrimaryTarget().getIndex());
				aPrimaryCopy = itsTargetConcept.getPrimaryTarget().copy();
				break;
			}
			case DOUBLE_CORRELATION :
			{
				aPrimaryCopy = itsTargetConcept.getPrimaryTarget().copy();
				aSecondaryCopy = itsTargetConcept.getSecondaryTarget().copy();
			}
			case MULTI_LABEL :
			{
				List<Column> aTemp = itsTargetConcept.getMultiTargets();
				for (Column c : aTemp)
					aMultiCopy.add(c.copy());
			}
			default : ;
		}
		
		// Generate swap randomized random results
		Log.COMMANDLINELOG = false;
		for (int i=0; i<theNrRepetitions; i++)
		{
			itsTable.swapRandomizeTarget(itsTargetConcept);
			
			switch(itsTargetConcept.getTargetType())
			{
				case SINGLE_NOMINAL :
				{
					aSubgroupDiscovery = new SubgroupDiscovery(itsSearchParameters, itsTable, itsPositiveCount);
					break;
				}
				case SINGLE_NUMERIC:
				{
					aSubgroupDiscovery = new SubgroupDiscovery(itsSearchParameters, itsTable, itsTargetAverage);
					break;
				}
				case MULTI_LABEL :
				{
					aSubgroupDiscovery = new SubgroupDiscovery(itsSearchParameters, itsTable);
					break;
				}
				case DOUBLE_REGRESSION :
				{
					aSubgroupDiscovery = new SubgroupDiscovery(itsSearchParameters, itsTable, true);
					break;
				}
				case DOUBLE_CORRELATION :
				{
					aSubgroupDiscovery = new SubgroupDiscovery(itsSearchParameters, itsTable, false);
					break;
				}
				default : ; // TODO should never get here, throw warning
			}
			
			aSubgroupDiscovery.Mine(System.currentTimeMillis());
			SubgroupSet aSubgroupSet = aSubgroupDiscovery.getResult();
			if (aSubgroupSet.size()==0)
				i--; // if no subgroups are found, try again.
			else
			{
				Log.COMMANDLINELOG = true;
				aQualities[i] = aSubgroupSet.getBestSubgroup().getMeasureValue();
				Log.logCommandLine((i + 1) + "," + aQualities[i]);
				Log.COMMANDLINELOG = false;
			}
		}
		
		// Restore swap randomized target columns to obtain the original dataset again
		switch(itsTargetConcept.getTargetType())
		{
			case DOUBLE_CORRELATION :
			{
				itsTargetConcept.setSecondaryTarget(aSecondaryCopy); // do NOT break; primary target needs restoration too
			}
			case SINGLE_NOMINAL :
			case SINGLE_NUMERIC :
			{
				itsTargetConcept.setPrimaryTarget(aPrimaryCopy);
				break;
			}
			case MULTI_LABEL :
			{
				itsTargetConcept.setMultiTargets(aMultiCopy);
				break;
			}
			default : ;
		}
		
		// Reset COMMANDLINELOG, return result
		Log.COMMANDLINELOG = aCOMMANDLINELOGmem;
		return aQualities;
	}

	public double performRegressionTest(double[] theQualities, int theK, SubgroupSet theSubgroupSet)
	{
		// extract average quality of top-k subgroups
		Iterator<Subgroup> anIterator = theSubgroupSet.iterator();
		double aTopKQuality = 0.0;
		for (int i=0; i<theK; i++)
		{
			Subgroup aSubgroup = anIterator.next();
			aTopKQuality += aSubgroup.getMeasureValue();
		}
		aTopKQuality /= ((double) theK);

		// make deep copy of double array
		int theNrRandomSubgroups = theQualities.length;
		double[] aCopy = new double[theNrRandomSubgroups];
		for (int i=0; i<theNrRandomSubgroups; i++)
			aCopy[i] = theQualities[i];

		// rescale all qualities between 0 and 1
		// also compute some necessary statistics
		Arrays.sort(aCopy);

		double aMin = Math.min(aCopy[0], aTopKQuality);
		double aMax = Math.max(aCopy[theNrRandomSubgroups-1], aTopKQuality);
		double xBar = 0.5; // given our scaling this always holds
		double yBar = 0.0; // initial value
		for (int i=0; i<theNrRandomSubgroups; i++)
		{
			aCopy[i] = (aCopy[i]-aMin)/(aMax-aMin);
			yBar += aCopy[i];
		}
		aTopKQuality = (aTopKQuality-aMin)/(aMax-aMin);
		yBar = (yBar+aTopKQuality)/((double) theNrRandomSubgroups + 1);

		// perform least squares linear regression on equidistant x-values and computed y-values
		double xxBar = 0.25; // initial value: this equals the square of (the x-value of our subgroup minus xbar)
		double xyBar = 0.5 * (aTopKQuality - yBar);
		double[] anXs = new double[theNrRandomSubgroups];
		for (int i=0; i<theNrRandomSubgroups; i++)
		{
			anXs[i] = ((double)i) / ((double)theNrRandomSubgroups);
			Log.logCommandLine("" + anXs[i] + "\t" + aCopy[i]);
		}

		for (int i=0; i<theNrRandomSubgroups; i++)
		{
			xxBar += (anXs[i] - xBar) * (anXs[i] - xBar);
			xyBar += (anXs[i] - xBar) * (aCopy[i] - yBar);
		}
		double beta1 = xyBar / xxBar;
		double beta0 = yBar - beta1 * xBar;
		// this gives us the regression line y = beta1 * x + beta0
		Log.logCommandLine("Fitted regression line: y = " + beta1 + " * x + " + beta0);
		double aScore = aTopKQuality - beta1 - beta0; // the regression test score now equals the average quality of the top-k subgroups, minus the regression value at x=1.
		Log.logCommandLine("Regression test score: " + aScore);
		return aScore;
	}

	public double[] performRegressionTest(double[] theQualities, SubgroupSet theSubgroupSet)
	{
		double aOne = performRegressionTest(theQualities, 1, theSubgroupSet);
		double aTen = Math.PI;
		if (theSubgroupSet.size()>=10)
			aTen = performRegressionTest(theQualities, 10, theSubgroupSet);
		double[] aResult = {aOne, aTen};
		return aResult;
	}

	public double computeEmpiricalPValue(double[] theQualities, SubgroupSet theSubgroupSet)
	{
		//hardcoded
		int aK = 1;

		// extract average quality of top-k subgroups
		Iterator<Subgroup> anIterator = theSubgroupSet.iterator();
		double aTopKQuality = 0.0;
		for (int i=0; i<aK; i++)
		{
			Subgroup aSubgroup = anIterator.next();
			aTopKQuality += aSubgroup.getMeasureValue();
		}
		aTopKQuality /= aK;

		int aCount = 0;
		for (double aQuality : theQualities)
			if (aQuality > aTopKQuality)
				aCount++;

		Arrays.sort(theQualities);
		Log.logCommandLine("Empirical p-value: " + aCount/(double)theQualities.length);
		Log.logCommandLine("score at alpha = 1%: " + theQualities[theQualities.length-theQualities.length/100]);
		Log.logCommandLine("score at alpha = 5%: " + theQualities[theQualities.length-theQualities.length/20]);
		Log.logCommandLine("score at alpha = 10%: " + theQualities[theQualities.length-theQualities.length/10]);
		return aCount/(double)theQualities.length;
	}

	public ConditionList getRandomConditionList(int theDepth, Random theRandom)
	{
		ConditionList aCL = new ConditionList();

		int aDepth = 1+theRandom.nextInt(theDepth); //random nr between 1 and theDepth (incl)
		int aNrColumns = itsTable.getNrColumns();

		for (int j = 0; j < aDepth; j++) // j conditions
		{
/*
			Attribute anAttribute = itsTable.getAttribute(theRandom.nextInt(aNrColumns));
			while (itsTargetConcept.isTargetAttribute(anAttribute))
			{
				anAttribute = itsTable.getAttribute(theRandom.nextInt(aNrColumns));
			}
			Attribute anAttribute;
			do
				anAttribute = itsTable.getAttribute(theRandom.nextInt(aNrColumns));
			while (itsTargetConcept.isTargetAttribute(anAttribute));
 */
			Column aColumn;
			do
				aColumn = itsTable.getColumn(theRandom.nextInt(aNrColumns));
			while (itsTargetConcept.isTargetAttribute(aColumn));

			int anOperator;
			Condition aCondition;
			switch(aColumn.getType())
			{
				case BINARY :
				{
					anOperator = Condition.EQUALS;
					aCondition = new Condition(aColumn, anOperator);
					aCondition.setValue(theRandom.nextBoolean() ? "1" : "0");
					break;
				}
				case NOMINAL :
				{
					anOperator = Condition.EQUALS;
					aCondition = new Condition(aColumn, anOperator);
					TreeSet<String> aDomain = aColumn.getDomain();
					int aNrDistinct = aDomain.size();
					int aRandomIndex = (int) (theRandom.nextDouble()* (double) aNrDistinct);
					Iterator<String> anIterator = aDomain.iterator();
					String aValue = anIterator.next();
					for (int i=0; i<aRandomIndex; i++)
						aValue = anIterator.next();
					aCondition.setValue(aValue);
					break;
				}
				case NUMERIC :
				default :
				{
					anOperator = theRandom.nextBoolean() ?
						Condition.LESS_THAN_OR_EQUAL : Condition.GREATER_THAN_OR_EQUAL;
					aCondition = new Condition(aColumn, anOperator);
					float aMin = aColumn.getMin();
					float aMax = aColumn.getMax();
					aCondition.setValue(
						Float.toString(aMin + (aMax - aMin) / 4 + (aMax - aMin) * theRandom.nextFloat() / 2));
					break;
				}
			}
			aCL.addCondition(aCondition);
		}
		return aCL;
	}
}
