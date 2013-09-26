/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    SetInstancesPanel.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.FileSourcedConverter;
import weka.core.converters.IncrementalConverter;
import weka.core.converters.URLSourcedLoader;

/** 
 * A panel that displays an instance summary for a set of instances and
 * lets the user open a set of instances from either a file or URL.
 *
 * Instances may be obtained either in a batch or incremental fashion.
 * If incremental reading is used, then
 * the client should obtain the Loader object (by calling
 * getLoader()) and read the instances one at a time. If
 * batch loading is used, then SetInstancesPanel will load
 * the data into memory inside of a separate thread and notify
 * the client when the operation is complete. The client can
 * then retrieve the instances by calling getInstances().
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 6890 $
 */
public class SetInstancesPanel
  extends JPanel {

  /** for serialization. */
  private static final long serialVersionUID = -384804041420453735L;

  /** the text denoting "no class" in the class combobox. */
  public final static String NO_CLASS = "No class";
  
  /** Click to open instances from a file. */
  protected JButton m_OpenFileBut = new JButton("Open file...");

  /** Click to open instances from a URL. */
  protected JButton m_OpenURLBut = new JButton("Open URL...");

  /** Click to close the dialog. */
  protected JButton m_CloseBut = new JButton("Close");

  /** The instance summary component. */
  protected InstancesSummaryPanel m_Summary = new InstancesSummaryPanel();

  /** the label for the class combobox. */
  protected JLabel m_ClassLabel = new JLabel("Class");
  
  /** the class combobox. */
  protected JComboBox m_ClassComboBox = new JComboBox(new DefaultComboBoxModel(new String[]{NO_CLASS}));
  
  /** The file chooser for selecting arff files. */
  protected ConverterFileChooser m_FileChooser
    = new ConverterFileChooser(new File(System.getProperty("user.dir")));

  /** Stores the last URL that instances were loaded from. */
  protected String m_LastURL = "http://";

  /** The thread we do loading in. */
  protected Thread m_IOThread;

  /**
   * Manages sending notifications to people when we change the set of
   * working instances.
   */
  protected PropertyChangeSupport m_Support = new PropertyChangeSupport(this);

  /** The current set of instances loaded. */
  protected Instances m_Instances;

  /** The current loader used to obtain the current instances. */
  protected weka.core.converters.Loader m_Loader;
  
  /** the parent frame. if one is provided, the close-button is displayed */
  protected JFrame m_ParentFrame = null;

  /** the panel the Close-Button is located in. */
  protected JPanel m_CloseButPanel = null;

  /** whether to read the instances incrementally, if possible. */
  protected boolean m_readIncrementally = true;
  
  /** whether to display zero instances as unknown ("?"). */
  protected boolean m_showZeroInstancesAsUnknown = false;
  
  /** whether to display a combobox that allows the user to choose the class
   * attribute. */
  protected boolean m_showClassComboBox;
  
  /**
   * Default constructor.
   */
  public SetInstancesPanel() {
    this(false, false);
  }
  
  /**
   * Create the panel.
   * 
   * @param showZeroInstancesAsUnknown	whether to display zero instances
   * 					as unknown (e.g., when reading data
   * 					incrementally)
   * @param showClassComboBox		whether to display a combobox
   * 					allowing the user to choose the class
   * 					attribute
   */
  public SetInstancesPanel(boolean showZeroInstancesAsUnknown, boolean showClassComboBox) {
    m_showZeroInstancesAsUnknown = showZeroInstancesAsUnknown;
    m_showClassComboBox = showClassComboBox;
    
    m_OpenFileBut.setToolTipText("Open a set of instances from a file");
    m_OpenURLBut.setToolTipText("Open a set of instances from a URL");
    m_CloseBut.setToolTipText("Closes the dialog");
    m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    m_OpenURLBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	setInstancesFromURLQ();
      }
    });
    m_OpenFileBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	setInstancesFromFileQ();
      }
    });
    m_CloseBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeFrame();
      }
    });
    m_Summary.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    m_ClassComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if ((m_Instances != null) && (m_ClassComboBox.getSelectedIndex() != -1)) {
	  if (m_Instances.numAttributes() >= m_ClassComboBox.getSelectedIndex()) {
	    m_Instances.setClassIndex(m_ClassComboBox.getSelectedIndex() - 1);   // -1 because of NO_CLASS element
	    m_Support.firePropertyChange("", null, null);
	  }
	}
      }
    });
    
    JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panelButtons.add(m_OpenFileBut);
    panelButtons.add(m_OpenURLBut);

    JPanel panelClass = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panelClass.add(m_ClassLabel);
    panelClass.add(m_ClassComboBox);

    JPanel panelButtonsAndClass;
    if (m_showClassComboBox) {
      panelButtonsAndClass = new JPanel(new GridLayout(2, 1));
      panelButtonsAndClass.add(panelButtons);
      panelButtonsAndClass.add(panelClass);
    }
    else {
      panelButtonsAndClass = new JPanel(new GridLayout(1, 1));
      panelButtonsAndClass.add(panelButtons);
    }
    
    m_CloseButPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    m_CloseButPanel.add(m_CloseBut);
    m_CloseButPanel.setVisible(false);
    
    JPanel panelButtonsAll = new JPanel(new BorderLayout());
    panelButtonsAll.add(panelButtonsAndClass, BorderLayout.CENTER);
    panelButtonsAll.add(m_CloseButPanel, BorderLayout.SOUTH);
    
    setLayout(new BorderLayout());
    add(m_Summary, BorderLayout.CENTER);
    add(panelButtonsAll, BorderLayout.SOUTH);
  }

  /**
   * Sets the frame, this panel resides in. Used for displaying the close 
   * button, i.e., the close-button is visible if the given frame is not null.
   * @param parent        the parent frame
   */
  public void setParentFrame(JFrame parent) {
    m_ParentFrame = parent;
    m_CloseButPanel.setVisible(m_ParentFrame != null);
  }
  
  /**
   * Returns the current frame the panel knows of, that it resides in. Can be
   * null.
   * @return the current parent frame
   */
  public JFrame getParentFrame() {
    return m_ParentFrame;
  }

  /**
   * closes the frame, i.e., the visibility is set to false.
   */
  public void closeFrame() {
    if (m_ParentFrame != null)
      m_ParentFrame.setVisible(false);
  }

  /**
   * Queries the user for a file to load instances from, then loads the
   * instances in a background process. This is done in the IO
   * thread, and an error message is popped up if the IO thread is busy.
   */
  public void setInstancesFromFileQ() {
    
    if (m_IOThread == null) {
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
	final File selected = m_FileChooser.getSelectedFile();
	m_IOThread = new Thread() {
	  public void run() {
	    setInstancesFromFile(selected);
	    m_IOThread = null;
	  }
	};
	m_IOThread.setPriority(Thread.MIN_PRIORITY); // UI has most priority
	m_IOThread.start();
      }
    } else {
      JOptionPane.showMessageDialog(this,
				    "Can't load at this time,\n"
				    + "currently busy with other IO",
				    "Load Instances",
				    JOptionPane.WARNING_MESSAGE);
    }
  }
    
  /**
   * Queries the user for a URL to load instances from, then loads the
   * instances in a background process. This is done in the IO
   * thread, and an error message is popped up if the IO thread is busy.
   */
  public void setInstancesFromURLQ() {
    
    if (m_IOThread == null) {
      try {
	String urlName = (String) JOptionPane.showInputDialog(this,
			"Enter the source URL",
			"Load Instances",
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			m_LastURL);
	if (urlName != null) {
	  m_LastURL = urlName;
	  final URL url = new URL(urlName);
	  m_IOThread = new Thread() {
	    public void run() {
	      setInstancesFromURL(url);
	      m_IOThread = null;
	    }
	  };
	  m_IOThread.setPriority(Thread.MIN_PRIORITY); // UI has most priority
	  m_IOThread.start();
	}
      } catch (Exception ex) {
	JOptionPane.showMessageDialog(this,
				      "Problem with URL:\n"
				      + ex.getMessage(),
				      "Load Instances",
				      JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this,
				    "Can't load at this time,\n"
				    + "currently busy with other IO",
				    "Load Instances",
				    JOptionPane.WARNING_MESSAGE);
    }
  }
  
  /**
   * Loads results from a set of instances contained in the supplied
   * file.
   *
   * @param f a value of type 'File'
   */
  protected void setInstancesFromFile(File f) {
    boolean incremental = m_readIncrementally;
    
    try {
      m_Loader = ConverterUtils.getLoaderForFile(f);
      if (m_Loader == null)
	throw new Exception("No suitable FileSourcedConverter found for file!\n" + f);
      
      // not an incremental loader?
      if (!(m_Loader instanceof IncrementalConverter))
	incremental = false;

      // load
      ((FileSourcedConverter) m_Loader).setFile(f);
      if (incremental) {
        m_Summary.setShowZeroInstancesAsUnknown(m_showZeroInstancesAsUnknown);
	setInstances(m_Loader.getStructure());
      } else {
        // If we are batch loading then we will know for sure that
        // the data has no instances
        m_Summary.setShowZeroInstancesAsUnknown(false);
	setInstances(m_Loader.getDataSet());
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
				    "Couldn't read from file:\n"
				    + f.getName(),
				    "Load Instances",
				    JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Loads instances from a URL.
   *
   * @param u the URL to load from.
   */
  protected void setInstancesFromURL(URL u) {
    boolean incremental = m_readIncrementally;
    
    try {
      m_Loader = ConverterUtils.getURLLoaderForFile(u.toString());
      if (m_Loader == null)
	throw new Exception("No suitable URLSourcedLoader found for URL!\n" + u);
      
      // not an incremental loader?
      if (!(m_Loader instanceof IncrementalConverter))
	incremental = false;

      // load
      ((URLSourcedLoader) m_Loader).setURL(u.toString());
      if (incremental) {
        m_Summary.setShowZeroInstancesAsUnknown(m_showZeroInstancesAsUnknown);
	setInstances(m_Loader.getStructure());
      } else {
        m_Summary.setShowZeroInstancesAsUnknown(false);
	setInstances(m_Loader.getDataSet());
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
				    "Couldn't read from URL:\n"
				    + u,
				    "Load Instances",
				    JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Updates the set of instances that is currently held by the panel.
   *
   * @param i a value of type 'Instances'
   */
  public void setInstances(Instances i) {

    m_Instances = i;
    m_Summary.setInstances(m_Instances);
    
    if (m_showClassComboBox) {
      DefaultComboBoxModel model = (DefaultComboBoxModel) m_ClassComboBox.getModel();
      model.removeAllElements();
      model.addElement(NO_CLASS);
      for (int n = 0; n < m_Instances.numAttributes(); n++) {
	Attribute att = m_Instances.attribute(n);
	String type = "(" + Attribute.typeToStringShort(att) + ")";
	model.addElement(type + " " + att.name());
      }
      if (m_Instances.classIndex() == -1)
	m_ClassComboBox.setSelectedIndex(m_Instances.numAttributes());
      else
	m_ClassComboBox.setSelectedIndex(m_Instances.classIndex() + 1);   // +1 because of NO_CLASS element
    }
    
    // Fire property change event for those interested.
    m_Support.firePropertyChange("", null, null);
  }

  /**
   * Gets the set of instances currently held by the panel.
   *
   * @return a value of type 'Instances'
   */
  public Instances getInstances() {
    
    return m_Instances;
  }

  /**
   * Returns the currently selected class index.
   * 
   * @return		the class index, -1 if none selected
   */
  public int getClassIndex() {
    if (m_ClassComboBox.getSelectedIndex() <= 0)
      return -1;
    else
      return m_ClassComboBox.getSelectedIndex() - 1;
  }
  
  /**
   * Gets the currently used Loader.
   *
   * @return a value of type 'Loader'
   */
  public weka.core.converters.Loader getLoader() {
    return m_Loader;
  }

  /**
   * Gets the instances summary panel associated with
   * this panel.
   * 
   * @return the instances summary panel
   */
  public InstancesSummaryPanel getSummary() {
    return m_Summary;
  }

  /**
   * Sets whether or not instances should be read incrementally
   * by the Loader. If incremental reading is used, then
   * the client should obtain the Loader object (by calling
   * getLoader()) and read the instances one at a time. If
   * batch loading is used, then SetInstancesPanel will load
   * the data into memory inside of a separate thread and notify
   * the client when the operation is complete. The client can
   * then retrieve the instances by calling getInstances().
   *
   * @param incremental true if instances are to be read incrementally
   * 
   */
  public void setReadIncrementally(boolean incremental) {
    m_readIncrementally = incremental;
  }

  /**
   * Gets whether instances are to be read incrementally or not.
   *
   * @return true if instances are to be read incrementally
   */
  public boolean getReadIncrementally() {
    return m_readIncrementally;
  }
  
  /**
   * Adds a PropertyChangeListener who will be notified of value changes.
   *
   * @param l a value of type 'PropertyChangeListener'
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    m_Support.addPropertyChangeListener(l);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param l a value of type 'PropertyChangeListener'
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    m_Support.removePropertyChangeListener(l);
  }
}
