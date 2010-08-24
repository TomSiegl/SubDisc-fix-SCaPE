package nl.liacs.subdisc;

import nl.liacs.subdisc.TargetConcept.TargetType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SearchParameters contains all search parameters for an experiment.
 */
public class SearchParameters implements XMLNodeInterface
{
	// when adding/removing members be sure to update addNodeTo() and loadNode()
	private TargetConcept	itsTargetConcept;
	private int				itsQualityMeasure;
	private float			itsQualityMeasureMinimum;

	private int				itsSearchDepth;
	private int				itsMinimumCoverage;
	private float			itsMaximumCoverage;
	private int				itsMaximumSubgroups;
	private float			itsMaximumTime;

	private int				itsSearchStrategy;
	private int				itsSearchStrategyWidth;
	private NumericStrategy	itsNumericStrategy;

	private int				itsNrSplitPoints;
	private float			itsAlpha;
	private float			itsBeta;
	private int				itsPostProcessingCount;
	private int				itsMaximumPostProcessingSubgroups;

	public static enum NumericStrategy
	{
		NUMERIC_BINS("bins"), NUMERIC_BEST("best"), NUMERIC_ALL("all");

		public final String TEXT;

		private NumericStrategy(String theText) { TEXT = theText; }

		public static NumericStrategy getNumericStrategy(String theString)
		{
			for(NumericStrategy n : NumericStrategy.values())
				if(n.TEXT.equalsIgnoreCase(theString))
					return n;
			return NUMERIC_BINS;	// TODO default, could throw not found dialog
		}
	}

	public SearchParameters(Node theSearchParametersNode)
	{
		if(theSearchParametersNode == null)
			return;	// TODO throw warning dialog
		loadData(theSearchParametersNode);
	}

	public SearchParameters() {}	// TODO for now

	/* QUALITY MEASURE */
	public TargetConcept getTargetConcept() { return itsTargetConcept; }
	public void setTargetConcept(TargetConcept theTargetConcept) { itsTargetConcept = theTargetConcept; }
	public TargetType getTargetType() { return itsTargetConcept.getTargetType(); }
	public int getQualityMeasure() { return itsQualityMeasure; }
	public String getQualityMeasureString() { return QualityMeasure.getMeasureString(itsQualityMeasure); }
	public float getQualityMeasureMinimum() { return itsQualityMeasureMinimum; }
	public void setQualityMeasureMinimum(float theQualityMeasureMinimum) { itsQualityMeasureMinimum = theQualityMeasureMinimum; }
	public void setQualityMeasure(String theQualityMeasure) { itsQualityMeasure = QualityMeasure.getMeasureCode(theQualityMeasure); }
	public void setQualityMeasure(int theQualityMeasure) { itsQualityMeasure = theQualityMeasure; }

	/* SEARCH CONDITIONS */
	public int getSearchDepth() { return itsSearchDepth; }
	public void setSearchDepth(int theSearchDepth) { itsSearchDepth = theSearchDepth; }
	public int getMinimumCoverage() { return itsMinimumCoverage; }
	public void setMinimumCoverage(int theMinimumCoverage) { itsMinimumCoverage = theMinimumCoverage; }
	public float getMaximumCoverage() { return itsMaximumCoverage; }
	public void setMaximumCoverage(float theMaximumCoverage) { itsMaximumCoverage = theMaximumCoverage; }
	public int getMaximumSubgroups() { return itsMaximumSubgroups; }
	public void setMaximumSubgroups(int theMaximumSubgroups) { itsMaximumSubgroups  = theMaximumSubgroups; }
	public float getMaximumTime() { return itsMaximumTime; }
	public void setMaximumTime(float theMaximumTime) { itsMaximumTime = theMaximumTime; }

	/* SEARCH STRATEGY */
	public int getSearchStrategy() { return itsSearchStrategy; }

	public static String getSearchStrategyName(int theSearchStrategy)
	{
		switch(theSearchStrategy)
		{
			case CandidateQueue.BFS		: return "breadth first";
			case CandidateQueue.DFS		: return "depth first";
			case CandidateQueue.BESTFIRST	: return "best first";
			case CandidateQueue.BEAM	: return "beam";
			default						: return "";
		}
	}

	public void setSearchStrategy(String theSearchStrategyName)
	{
		if (theSearchStrategyName.equals("breadth first"))
			itsSearchStrategy = CandidateQueue.BFS;
		else if (theSearchStrategyName.equals("depth first"))
			itsSearchStrategy = CandidateQueue.DFS;
		else if (theSearchStrategyName.equals("best first"))
			itsSearchStrategy = CandidateQueue.BESTFIRST;
		else if (theSearchStrategyName.equals("beam"))
			itsSearchStrategy = CandidateQueue.BEAM;
		else
			itsSearchStrategy = CandidateQueue.BESTFIRST; // default
	}

	public NumericStrategy getNumericStrategy() { return itsNumericStrategy; }

	public void setNumericStrategy(String theNumericStrategy)
	{
/*
		// TODO not safe if theNumericStrategy is 'illegal'
		for(NumericStrategy n : NumericStrategy.values())
			if(n.text.equalsIgnoreCase(theNumericStrategy))
				itsNumericStrategy = n;
*/		if(theNumericStrategy.equalsIgnoreCase("all"))
			itsNumericStrategy = NumericStrategy.NUMERIC_ALL;
		else if(theNumericStrategy.equalsIgnoreCase("bin"))
			itsNumericStrategy = NumericStrategy.NUMERIC_BINS;
		else
			itsNumericStrategy = NumericStrategy.NUMERIC_BEST;
	}

	public void setSearchStrategyWidth(int theWidth)	{ itsSearchStrategyWidth = theWidth; }
	public int getSearchStrategyWidth()		{ return itsSearchStrategyWidth; }
	public int getNrSplitPoints()			{ return itsNrSplitPoints; }
	public void setNrSplitPoints(int theNr)	{ itsNrSplitPoints = theNr; }
	public float getAlpha()					{ return itsAlpha; }
	public void setAlpha(float theAlpha)	{ itsAlpha = theAlpha; }
	public float getBeta()					{ return itsBeta; }
	public void setBeta(float theBeta)		{ itsBeta = theBeta; }
	public int getPostProcessingCount()		{ return itsPostProcessingCount; }
	public void setPostProcessingCount(int theNr)	{ itsPostProcessingCount = theNr; }
	public int getMaximumPostProcessingSubgroups()	{ return itsMaximumPostProcessingSubgroups; }
	public void setMaximumPostProcessingSubgroups(int theNr)	{ itsMaximumPostProcessingSubgroups = theNr; }

	@Override
	public void addNodeTo(Node theParentNode)
	{
		Node aNode = XMLNode.addNodeTo(theParentNode, "search_parameters");
		XMLNode.addNodeTo(aNode, "quality_measure", getQualityMeasureString());
		XMLNode.addNodeTo(aNode, "quality_measure_minimum", getQualityMeasureMinimum());
		XMLNode.addNodeTo(aNode, "search_depth", getSearchDepth());
		XMLNode.addNodeTo(aNode, "minimum_coverage", getMinimumCoverage());
		XMLNode.addNodeTo(aNode, "maximum_coverage", getMaximumCoverage());
		XMLNode.addNodeTo(aNode, "maximum_subgroups", getMaximumSubgroups());
		XMLNode.addNodeTo(aNode, "maximum_time", getMaximumTime());
		XMLNode.addNodeTo(aNode, "search_strategy", getSearchStrategyName(getSearchStrategy()));
		XMLNode.addNodeTo(aNode, "search_strategy_width", getSearchStrategyWidth());
		XMLNode.addNodeTo(aNode, "numeric_strategy", getNumericStrategy().TEXT);
		XMLNode.addNodeTo(aNode, "nr_split_points", getNrSplitPoints());
		XMLNode.addNodeTo(aNode, "alpha", getAlpha());
		XMLNode.addNodeTo(aNode, "beta", getBeta());
		XMLNode.addNodeTo(aNode, "post_processing_count", getPostProcessingCount());
		XMLNode.addNodeTo(aNode, "maximum_post_processing_subgroups", getMaximumPostProcessingSubgroups());
	}

	private void loadData(Node theSearchParametersNode)
	{
		NodeList aChildren = theSearchParametersNode.getChildNodes();
		for(int i = 0, j = aChildren.getLength(); i < j; ++i)
		{
			Node aSetting = aChildren.item(i);
			String aNodeName = aSetting.getNodeName();
			if("quality_measure".equalsIgnoreCase(aNodeName))
				itsQualityMeasure = QualityMeasure.getMeasureCode(aSetting.getTextContent());
			else if("quality_measure_minimum".equalsIgnoreCase(aNodeName))
				itsQualityMeasureMinimum = Float.parseFloat(aSetting.getTextContent());
			else if("search_depth".equalsIgnoreCase(aNodeName))
				itsSearchDepth = Integer.parseInt(aSetting.getTextContent());
			else if("minimum_coverage".equalsIgnoreCase(aNodeName))
				itsMinimumCoverage = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_coverage".equalsIgnoreCase(aNodeName))
				itsMaximumCoverage = Float.parseFloat(aSetting.getTextContent());
			else if("maximum_subgroups".equalsIgnoreCase(aNodeName))
				itsMaximumSubgroups = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_time".equalsIgnoreCase(aNodeName))
				itsMaximumTime = Float.parseFloat(aSetting.getTextContent());
			else if("search_strategy".equalsIgnoreCase(aNodeName))
				setSearchStrategy(aSetting.getTextContent());
			else if("search_strategy_width".equalsIgnoreCase(aNodeName))
				itsSearchStrategyWidth = Integer.parseInt(aSetting.getTextContent());
			else if("numeric_strategy".equalsIgnoreCase(aNodeName))
				itsNumericStrategy = (NumericStrategy.getNumericStrategy(aSetting.getTextContent()));
			else if("nr_split_points".equalsIgnoreCase(aNodeName))
				itsNrSplitPoints = Integer.parseInt(aSetting.getTextContent());
			else if("alpha".equalsIgnoreCase(aNodeName))
				itsAlpha = Float.parseFloat(aSetting.getTextContent());
			else if("beta".equalsIgnoreCase(aNodeName))
				itsBeta = Float.parseFloat(aSetting.getTextContent());
			else if("post_processing_count".equalsIgnoreCase(aNodeName))
				itsPostProcessingCount = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_post_processing_subgroups".equalsIgnoreCase(aNodeName))
				itsMaximumPostProcessingSubgroups = Integer.parseInt(aSetting.getTextContent());
			else
				;	// TODO throw warning dialog
		}
	}

}
