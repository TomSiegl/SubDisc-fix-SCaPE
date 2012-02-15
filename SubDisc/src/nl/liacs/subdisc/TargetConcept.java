package nl.liacs.subdisc;

import java.util.*;

import org.w3c.dom.*;

/**
 * Depending on the {@link TargetType TargetType} of a TargetConcept, it holds
 * the <code>PrimaryTarget</code> and/or <code>SecondaryTarget</code>/<code>
 * MultiTargets</code>. The TargetType indicates what type of search setting
 * will be used in the experiment. All TargetConcept constructors and setters
 * ensure that its TargetType is never <code>null</code> (<code>SINGLE_NOMINAL
 * </code> by default).
 */
public class TargetConcept implements XMLNodeInterface
{

	// when adding/removing members be sure to update addNodeTo() and loadNode()
	// itsMembers
	private int		itsNrTargetAttributes = 1;	// always 1 in current code
	private TargetType	itsTargetType;
	private Column		itsPrimaryTarget;
	private String		itsTargetValue;
	private Column		itsSecondaryTarget;
	private List<Column>	itsMultiRegressionTargets;
	private List<Column>	itsMultiTargets;
	// for double regression code:
	private List<Column>	itsSecondaryTargets;
	private List<Column>	itsTertiaryTargets;
	private boolean		itsInterceptRelevance;
	private String		itsGlobalRegressionModel;

	public TargetConcept()
	{
		itsTargetType = TargetType.getDefault();
	}

	// creation of TargetConcept relies on Table being loaded first
	public TargetConcept(Node theTargetConceptNode, Table theTable)
	{
		if (theTargetConceptNode == null)
			return;	// TODO throw warning dialog

		NodeList aChildren = theTargetConceptNode.getChildNodes();
		for (int i = 0, j = aChildren.getLength(); i < j; ++i)
		{
			Node aSetting = aChildren.item(i);
			String aNodeName = aSetting.getNodeName();
			if ("nr_target_attributes".equalsIgnoreCase(aNodeName))
				itsNrTargetAttributes = Integer.parseInt(aSetting.getTextContent());
			if ("target_type".equalsIgnoreCase(aNodeName))
				itsTargetType = (TargetType.getTargetType(aSetting.getTextContent()));
			else if ("primary_target".equalsIgnoreCase(aNodeName))
				itsPrimaryTarget = theTable.getColumn(aSetting.getTextContent());
			else if ("target_value".equalsIgnoreCase(aNodeName))
				itsTargetValue = aSetting.getTextContent();
			else if ("secondary_target".equalsIgnoreCase(aNodeName))
				itsSecondaryTarget = theTable.getColumn(aSetting.getTextContent());
			else if ("multi_targets".equalsIgnoreCase(aNodeName))
			{
				if (!aSetting.getTextContent().isEmpty())
				{
					itsMultiTargets = new ArrayList<Column>();
					for (String s : aSetting.getTextContent().split(",", -1))
						itsMultiTargets.add(theTable.getColumn(s));
				}
			}
			else if ("multi_regression_targets".equalsIgnoreCase(aNodeName))
			{
				if (!aSetting.getTextContent().isEmpty())
				{
					itsSecondaryTargets = new ArrayList<Column>();
					itsTertiaryTargets = new ArrayList<Column>();
					for (String s : aSetting.getTextContent().split(",", -1))
						itsSecondaryTargets.add(theTable.getColumn(s));
				}
			}
			else
				;	// TODO throw warning dialog
		}
	}

	// member methods
	public int getNrTargetAttributes() { return itsNrTargetAttributes; }
	public void setNrTargetAttributes(int theNr) { itsNrTargetAttributes = theNr; }
	public TargetType getTargetType() { return itsTargetType; }
	public void setTargetType(String theTargetTypeName)
	{
			itsTargetType = TargetType.getTargetType(theTargetTypeName);
	}

	public Column getPrimaryTarget() { return itsPrimaryTarget; }
	public void setPrimaryTarget(Column thePrimaryTarget) { itsPrimaryTarget = thePrimaryTarget; }
	public String getTargetValue() { return itsTargetValue; }
	public void setTargetValue(String theTargetValue) { itsTargetValue = theTargetValue; }

	public Column getSecondaryTarget() { return itsSecondaryTarget; }
	public void setSecondaryTarget(Column theSecondaryTarget) { itsSecondaryTarget = theSecondaryTarget; }

	public List<Column> getMultiTargets() { return itsMultiTargets; }
	public void setMultiTargets(List<Column> theMultiTargets)
	{
		itsMultiTargets = theMultiTargets;
	}

	public List<Column> getMultiRegressionTargets() { return itsMultiRegressionTargets; }
	public void setMultiRegressionTargets(List<Column> theMultiRegressionTargets)
	{
		itsMultiRegressionTargets = theMultiRegressionTargets;
	}

	public List<Column> getSecondaryTargets() { return itsSecondaryTargets; }
	public void setSecondaryTargets(List<Column> theSecondaryTargets)
	{
		itsSecondaryTargets = theSecondaryTargets;
	}

	public List<Column> getTertiaryTargets() { return itsTertiaryTargets; }
	public void setTertiaryTargets(List<Column> theTertiaryTargets)
	{
		itsTertiaryTargets = theTertiaryTargets;
	}
	
	public int getNrTargets()
	{
		return 1+itsSecondaryTargets.size()+itsTertiaryTargets.size();
	}
	
	public boolean getInterceptRelevance() { return itsInterceptRelevance; }
	public void setInterceptRelevance(boolean theInterceptRelevance)
	{
		itsInterceptRelevance = theInterceptRelevance;
	}

	public boolean isSingleNominal() { return (itsTargetType == TargetType.SINGLE_NOMINAL); }

	public boolean isTargetAttribute(Column theColumn)
	{
		switch (itsTargetType)
		{
			case SINGLE_NOMINAL :
			case SINGLE_NUMERIC :
				return (theColumn == itsPrimaryTarget);
			case DOUBLE_REGRESSION :
				return (theColumn == itsPrimaryTarget || itsSecondaryTargets.contains(theColumn) || itsTertiaryTargets.contains(theColumn) );
			case DOUBLE_CORRELATION :
				return (theColumn == itsPrimaryTarget || theColumn == itsSecondaryTarget);
			case MULTI_LABEL :
				return (itsMultiTargets.contains(theColumn));
			default :
				return false;
		}
	}

	/**
	 * Creates an {@link XMLNode XMLNode} representation of this TargetConcept.
	 * @param theParentNode the Node of which this Node will be a ChildNode
//	 * @return a Node that contains all the information of this TargetConcept
	 */
	@Override
	public void addNodeTo(Node theParentNode)
	{
		Node aNode = XMLNode.addNodeTo(theParentNode, "target_concept");
		XMLNode.addNodeTo(aNode, "nr_target_attributes", itsNrTargetAttributes);
		XMLNode.addNodeTo(aNode, "target_type", itsTargetType.GUI_TEXT);

		if (itsPrimaryTarget == null)
			XMLNode.addNodeTo(aNode, "primary_target");
		else
			XMLNode.addNodeTo(aNode, "primary_target", itsPrimaryTarget.getName());

		XMLNode.addNodeTo(aNode, "target_value", itsTargetValue);

		if (itsSecondaryTarget == null)
			XMLNode.addNodeTo(aNode, "secondary_target");
		else
			XMLNode.addNodeTo(aNode, "secondary_target", itsSecondaryTarget.getName());

		if (itsMultiTargets == null || itsMultiTargets.size() == 0)
			XMLNode.addNodeTo(aNode, "multi_targets");
		else
		{
			StringBuilder sb = new StringBuilder(itsMultiTargets.size() * 10);
			for (Column c : itsMultiTargets)
				sb.append(c.getName() + ",");
			XMLNode.addNodeTo(aNode, "multi_targets", sb.substring(0, sb.length() - 1));
		}

		if (itsMultiRegressionTargets == null || itsMultiRegressionTargets.size() == 0)
			XMLNode.addNodeTo(aNode, "multi_regression_targets");
		else
		{
			StringBuilder sb = new StringBuilder(itsMultiRegressionTargets.size() * 10);
			for (Column c : itsMultiRegressionTargets)
				sb.append(c.getName() + ",");
			XMLNode.addNodeTo(aNode, "multi_regression_targets", sb.substring(0, sb.length() - 1));
		}

		if (itsSecondaryTargets == null || itsSecondaryTargets.size() == 0)
			XMLNode.addNodeTo(aNode, "secondary_targets");
		else
		{
			StringBuilder sb = new StringBuilder(itsSecondaryTargets.size() * 10);
			for (Column c : itsSecondaryTargets)
				sb.append(c.getName() + ",");
			XMLNode.addNodeTo(aNode, "secondary_targets", sb.substring(0, sb.length() - 1));
		}

		if (itsTertiaryTargets == null || itsTertiaryTargets.size() == 0)
			XMLNode.addNodeTo(aNode, "tertiary_targets");
		else
		{
			StringBuilder sb = new StringBuilder(itsTertiaryTargets.size() * 10);
			for (Column c : itsTertiaryTargets)
				sb.append(c.getName() + ",");
			XMLNode.addNodeTo(aNode, "tertiary_targets", sb.substring(0, sb.length() - 1));
		}
	}
	
	public String getGlobalRegressionModel() { return itsGlobalRegressionModel;	}
	public void setGlobalRegressionModel(String theModel) {	itsGlobalRegressionModel = theModel; }
}
