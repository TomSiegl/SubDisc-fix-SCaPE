package nl.liacs.subdisc.gui;

import java.awt.event.*;
import java.text.*;

import javax.swing.*;

public class RandomQualitiesWindow extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;

	public static final String RANDOM_SUBSETS = "Random subsets";
	public static final String RANDOM_DESCRIPTIONS = "Random descriptions";
	public static final String SWAP_RANDOMIZATION = "Swap-randomization";
	public static final String DEFAULT_AMOUNT = "1000";

	private ButtonGroup itsMethods;
	private JTextField itsAmountField;
	private String[] itsSettings;

	public RandomQualitiesWindow(String theSetting)
	{
		super.setModalityType(DEFAULT_MODALITY_TYPE);
		itsSettings = new String[] { theSetting, null };
		initComponents();
		setTitle("Which method?");
		setIconImage(MiningWindow.ICON);
		setLocation(100, 100);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	private void initComponents()
	{
		itsMethods = new ButtonGroup();

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel aMasterPanel = new JPanel();
		aMasterPanel.setLayout(new BoxLayout(aMasterPanel, BoxLayout.Y_AXIS));
		aMasterPanel.setBorder(GUI.buildBorder("Method to compute random qualities"));

		JPanel aRadioButtonPanel = new JPanel();
		aRadioButtonPanel.setLayout(new BoxLayout(aRadioButtonPanel, BoxLayout.Y_AXIS));

		JRadioButton aRadioButton = new JRadioButton(RANDOM_SUBSETS);
		aRadioButton.setActionCommand(RANDOM_SUBSETS);
		aRadioButtonPanel.add(aRadioButton);
		itsMethods.add(aRadioButton);

		aRadioButton = new JRadioButton(RANDOM_DESCRIPTIONS);
		aRadioButton.setActionCommand(RANDOM_DESCRIPTIONS);
		aRadioButtonPanel.add(aRadioButton);
		itsMethods.add(aRadioButton);
		aMasterPanel.add(aRadioButtonPanel);

		aRadioButton = new JRadioButton(SWAP_RANDOMIZATION);
		aRadioButton.setActionCommand(SWAP_RANDOMIZATION);
		aRadioButtonPanel.add(aRadioButton);
		itsMethods.add(aRadioButton);
		aMasterPanel.add(aRadioButtonPanel);

		JPanel aNumberPanel = new JPanel();
		aNumberPanel.add(GUI.buildLabel("Amount", itsAmountField));
		aNumberPanel.add(Box.createHorizontalStrut(50));
		aNumberPanel.add(itsAmountField = GUI.buildTextField(DEFAULT_AMOUNT));
		aNumberPanel.setAlignmentX(LEFT_ALIGNMENT);
		aMasterPanel.add(aNumberPanel);

		getContentPane().add(aMasterPanel);

		final JPanel aButtonPanel = new JPanel();
		aButtonPanel.add(GUI.buildButton("OK", 'O', "ok", this));
		aButtonPanel.add(GUI.buildButton("Cancel", 'C', "cancel", this));
		aButtonPanel.setAlignmentX(LEFT_ALIGNMENT);

		getContentPane().add(aButtonPanel);

		// select appropriate radio button, focus itsAmountField
		if (RANDOM_DESCRIPTIONS.equals(itsSettings[0]))
			((JRadioButton) aRadioButtonPanel.getComponent(1)).setSelected(true);
		else
			((JRadioButton) aRadioButtonPanel.getComponent(0)).setSelected(true);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				aButtonPanel.getComponent(0).requestFocusInWindow();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();
		if ("ok".equals(aCommand))
		{
			NumberFormat aFormat = NumberFormat.getNumberInstance();
			try
			{
				if (aFormat.parse(itsAmountField.getText()).intValue() <= 1)
					showErrorDialog("Amount must be > 1.");
				else
				{
					itsSettings[0] = itsMethods.getSelection().getActionCommand();
					itsSettings[1] = itsAmountField.getText();
					dispose();
				}
			}
			catch (ParseException e)
			{
				showErrorDialog(e.getMessage());
			}
		}
		else if ("cancel".equals(aCommand))
			dispose();
	}

	private void showErrorDialog(String theMessage)
	{
		JOptionPane.showMessageDialog(this, theMessage,"Invalid input", JOptionPane.ERROR_MESSAGE);
	}

	public String[] getSettings()
	{
		return itsSettings;
	}
}
