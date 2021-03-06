package rocks.inspectit.ui.rcp.composite;

import java.util.Objects;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.cs.storage.IStorageData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryChangeListener;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;
import rocks.inspectit.ui.rcp.storage.listener.StorageChangeListener;
import rocks.inspectit.ui.rcp.util.AccessibleArrowImage;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * A composite to be the head client of the form that
 * {@link rocks.inspectit.ui.rcp.editor.root.FormRootEditor} is made of.
 *
 * @author Ivan Senic
 *
 */
public class BreadcrumbTitleComposite extends Composite implements CmrRepositoryChangeListener, StorageChangeListener {

	/**
	 * Maximum text length for the label content.
	 */
	private static final int MAX_TEXT_LENGTH = 30;

	/**
	 * Arrow to be displayed between the breadcrumbs.
	 */
	private final Image arrow;

	/**
	 * {@link ToolBarManager} of the composite.
	 */
	private ToolBarManager toolBarManager;

	/**
	 * Displayed repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * Label for repository name.
	 */
	private CLabel repositoryLabel;

	/**
	 * Label for agent name.
	 */
	private CLabel agentLabel;

	/**
	 * Label for group description.
	 */
	private CLabel groupLabel;

	/**
	 * Label for view description.
	 */
	private CLabel viewLabel;

	/**
	 * Default constructor.
	 *
	 * @param parent
	 *            Parent composite.
	 * @param style
	 *            Style.
	 * @see Composite#Composite(Composite, int)
	 */
	public BreadcrumbTitleComposite(Composite parent, int style) {
		super(parent, style);
		arrow = new AccessibleArrowImage(true).createImage();
		init();
	}

	/**
	 * Initializes the widget.
	 */
	private void init() {
		// define layout
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		Composite breadcrumbComposite = new Composite(this, SWT.NONE);
		breadcrumbComposite.setLayout(new GridLayout(7, false));
		breadcrumbComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		repositoryLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		repositoryLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		agentLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		agentLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		groupLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		groupLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		new Label(breadcrumbComposite, SWT.NONE).setImage(arrow);

		viewLabel = new CLabel(breadcrumbComposite, SWT.NONE);
		viewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// create tool-bar and its manager
		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
		toolbar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		toolBarManager = new ToolBarManager(toolbar);
	}

	/**
	 * Gets {@link #toolBarManager}.
	 *
	 * @return {@link #toolBarManager}
	 */
	public ToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	/**
	 * Sets {@link #repositoryDefinition}.
	 *
	 * @param repositoryDefinition
	 *            New value for {@link #repositoryDefinition}
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
		repositoryLabel.setText(TextFormatter.clearLineBreaks(TextFormatter.crop(repositoryDefinition.getName(), MAX_TEXT_LENGTH)));
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			repositoryLabel.setImage(ImageFormatter.getCmrRepositoryImage((CmrRepositoryDefinition) repositoryDefinition, true));
			InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			repositoryLabel.setImage(ImageFormatter.getStorageRepositoryImage((StorageRepositoryDefinition) repositoryDefinition));
			InspectIT.getDefault().getInspectITStorageManager().addStorageChangeListener(this);
		}
	}

	/**
	 * Sets the agent name.
	 *
	 * @param agentName
	 *            Agent name.
	 * @param agentImg
	 *            Image to go next to the agent name. If <code>null</code> is passed no changed to
	 *            the current image will be done.
	 */
	public void setAgent(String agentName, Image agentImg) {
		agentLabel.setText(TextFormatter.clearLineBreaks(TextFormatter.crop(agentName, MAX_TEXT_LENGTH)));
		agentLabel.setToolTipText(agentName);
		if (null != agentImg) {
			agentLabel.setImage(agentImg);
		}
	}

	/**
	 * Sets the title text and image.
	 *
	 * @param group
	 *            Group description.
	 * @param groupdImg
	 *            Image to go next to the title. If <code>null</code> is passed no changed to the
	 *            current image will be done.
	 */
	public void setGroup(String group, Image groupdImg) {
		groupLabel.setText(TextFormatter.clearLineBreaks(TextFormatter.crop(group, MAX_TEXT_LENGTH)));
		groupLabel.setToolTipText(group);
		if (null != groupdImg) {
			groupLabel.setImage(groupdImg);
		}
		layoutInternal();
	}

	/**
	 * Sets the description.
	 *
	 * @param view
	 *            View description.
	 * @param viewImg
	 *            Image to go next to the view description. If <code>null</code> is passed no
	 *            changed to the current image will be done.
	 */
	public void setView(String view, Image viewImg) {
		viewLabel.setText(TextFormatter.clearLineBreaks(view));
		viewLabel.setToolTipText(view);
		if (null != viewImg) {
			viewLabel.setImage(viewImg);
		}
		layoutInternal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition cmrRepositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if ((newStatus != OnlineStatus.CHECKING) && Objects.equals(repositoryDefinition, cmrRepositoryDefinition)) {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					repositoryLabel.setImage(ImageFormatter.getCmrRepositoryImage((CmrRepositoryDefinition) repositoryDefinition, true));
				}
			}, getDisplay(), repositoryLabel);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (Objects.equals(repositoryDefinition, cmrRepositoryDefinition)) {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					repositoryLabel.setText(TextFormatter.clearLineBreaks(repositoryDefinition.getName()));
					layoutInternal();
				}
			}, getDisplay(), repositoryLabel);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
	}

	/**
	 * Layouts the widget so that the text is properly displayed on the composite.
	 */
	private void layoutInternal() {
		layout(true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageDataUpdated(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageRemotelyDeleted(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageLocallyDeleted(IStorageData storageData) {
		updateStorageDetailsIfDisplayed(storageData);
	}

	/**
	 * Updates storage name and icon if given storageData is displayed currently on the breadcrumb.
	 *
	 * @param storageData
	 *            {@link IStorageData}
	 */
	private void updateStorageDetailsIfDisplayed(IStorageData storageData) {
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			if (Objects.equals(storageRepositoryDefinition.getLocalStorageData(), storageData)) {
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						repositoryLabel.setText(TextFormatter.clearLineBreaks(repositoryDefinition.getName()));
						repositoryLabel.setImage(ImageFormatter.getStorageRepositoryImage(storageRepositoryDefinition));
						layoutInternal();
					}
				}, getDisplay(), repositoryLabel);
			}
		}
	}

	/**
	 * Returns textual representation of the displayed data for the copy purposes.
	 *
	 * @return Returns textual representation of the displayed data for the copy purposes.
	 */
	public String getCopyString() {
		StringBuilder sb = new StringBuilder();

		sb.append(repositoryLabel.getText());
		sb.append(" > ");
		sb.append(agentLabel.getText());
		if (null != groupLabel.getText()) {
			sb.append(" > ");
			sb.append(groupLabel.getText());
		}
		if (null != viewLabel.getText()) {
			sb.append(" > ");
			sb.append(viewLabel.getText());
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		arrow.dispose();
		InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().removeStorageChangeListener(this);
		super.dispose();
	}

}
