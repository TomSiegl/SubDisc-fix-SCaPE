package nl.liacs.subdisc.cui;

import java.io.*;
import java.util.*;

import nl.liacs.subdisc.*;

public enum Gene2CuiMap implements CuiMapInterface
{
	// entrez2cui = 64870, go2cui = 15879
	ENTREZ2CUI(CuiMapInterface.ENTREZ2CUI_PATH, CuiMapInterface.NR_ENTREZ_CUI),
	GO2CUI(CuiMapInterface.GO2CUI_PATH, CuiMapInterface.NR_GO_CUI);
//	ENSEMBL2CUI(CuiMapInterface.ENSEMBL2CUI_PATH);

	private final Map<String, String> itsGene2CuiMap;

	private Gene2CuiMap(String theFilePath, int theNrCuis)
	{
		File aFile = new File(theFilePath);

		if (!aFile.exists())
		{
			itsGene2CuiMap = null;
			ErrorLog.log(aFile, new FileNotFoundException(""));
			return;
		}
		else
		{
			itsGene2CuiMap = new HashMap<String, String>(theNrCuis);
			parseFile(aFile);
		}
	}

	private void parseFile(File theFile)
	{
		BufferedReader aReader = null;

		try
		{
			aReader = new BufferedReader(new FileReader(theFile));
			String aLine;
			String[] aLineArray;

			aReader.readLine();	// skip headerLine
			while ((aLine = aReader.readLine()) != null)
			{
				// TODO compare with speed of Scanner().
//				anIndexMap.put(aLine.split(",")[0], ++aLineNr);
				aLineArray = aLine.split(",");
				itsGene2CuiMap.put(aLineArray[0], aLineArray[1]);
			}
		}
		catch (FileNotFoundException e)
		{
			ErrorLog.log(theFile, e);
			return;
		}
		catch (IOException e)
		{
			ErrorLog.log(theFile, e);
			return;
		}
		finally
		{
			try
			{
				if (aReader != null)
					aReader.close();
			}
			catch (IOException e)
			{
				ErrorLog.log(theFile, e);
			}
		}
	}

	/**
	 * Returns the <code>Map<String, String></code> for this Gene2CuiMap.
	 * 
	 * @return the <code>Map<String, Integer></code> for this Gene2CuiMap, or
	 * <code>null</code> if there is none.
	 */
	public Map<String, String> getMap()
	{
		if (itsGene2CuiMap == null)
			return null;
		else
			return Collections.unmodifiableMap(itsGene2CuiMap);
	}
}