package com.dynamo.cr.parted.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dynamo.cr.editor.core.operations.IMergeableOperation;
import com.dynamo.cr.editor.core.operations.MergeableDelegatingOperationHistory;
import com.dynamo.cr.editor.core.operations.IMergeableOperation.Type;
import com.dynamo.cr.parted.curve.CurveEditor;
import com.dynamo.cr.parted.curve.HermiteSpline;
import com.dynamo.cr.parted.curve.ICurveProvider;
import com.dynamo.cr.properties.IPropertyDesc;
import com.dynamo.cr.properties.IPropertyModel;
import com.dynamo.cr.properties.IPropertyObjectWorld;
import com.dynamo.cr.properties.types.ValueSpread;
import com.dynamo.cr.sceneed.core.Node;

public class ParticleFXCurveEditorPage implements ICurveEditorPage, ISelectionListener, IOperationHistoryListener, ICheckStateListener {

    private static Logger logger = LoggerFactory
            .getLogger(ParticleFXCurveEditorPage.class);
    private CurveEditor curveEditor;
    private IWorkbenchPartSite site;
    private IUndoContext undoContext;
    private Node selectedNode;
    private Composite composite;
    private Set<Object> hidden = new HashSet<Object>();
    private CheckboxTableViewer list;
    private IOperationHistory history;
    @SuppressWarnings("unchecked")
    private IPropertyDesc<Node, ? extends IPropertyObjectWorld>[] input = new IPropertyDesc[0];
    @SuppressWarnings("unchecked")
    private IPropertyDesc<Node, ? extends IPropertyObjectWorld>[] oldInput = new IPropertyDesc[0];
    private UndoActionHandler undoHandler;
    private RedoActionHandler redoHandler;
    private Color[] colors = new Color[24];

    public ParticleFXCurveEditorPage(UndoActionHandler undoHandler, RedoActionHandler redoHandler) {
        this.undoHandler = undoHandler;
        this.redoHandler = redoHandler;
    }

    private Color getColor(Object element) {
        for (int i = 0; i < input.length; ++i) {
            if (element == input[i]) {
                int index = (i * colors.length) / input.length;
                return colors[index % colors.length];
            }
        }
        return null;
    }

    class Provider implements ICurveProvider {

        @SuppressWarnings("unchecked")
        @Override
        public HermiteSpline getSpline(int i) {
            IPropertyDesc<Node, IPropertyObjectWorld> pd = (IPropertyDesc<Node, IPropertyObjectWorld>) input[i];
            IPropertyModel<Node, IPropertyObjectWorld> propertyModel = ((IPropertyModel<Node, IPropertyObjectWorld>) selectedNode.getAdapter(IPropertyModel.class));
            ValueSpread vs = (ValueSpread) propertyModel.getPropertyValue(pd.getId());
            HermiteSpline spline = (HermiteSpline) vs.getCurve();
            return spline;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setSpline(HermiteSpline spline, int i, boolean intermediate) {
            IPropertyDesc<Node, IPropertyObjectWorld> pd = (IPropertyDesc<Node, IPropertyObjectWorld>) input[i];
            IPropertyModel<Node, IPropertyObjectWorld> propertyModel = ((IPropertyModel<Node, IPropertyObjectWorld>) selectedNode.getAdapter(IPropertyModel.class));
            ValueSpread vs = (ValueSpread) propertyModel.getPropertyValue(pd.getId());
            vs.setCurve(spline);
            boolean force = intermediate == false;
            IUndoableOperation operation = propertyModel.setPropertyValue(pd.getId(), vs, force);
            if (operation instanceof IMergeableOperation) {
                IMergeableOperation mo = (IMergeableOperation) operation;
                if (intermediate)
                    mo.setType(Type.INTERMEDIATE);
                else
                    mo.setType(Type.CLOSE);
            }

            operation.addContext(undoContext);
            IStatus status = null;
            try {
                status = history.execute(operation, null, null);
                if (status != Status.OK_STATUS) {
                    logger.error("Failed to execute operation", status.getException());
                    throw new RuntimeException(status.toString());
                }
            } catch (final ExecutionException e) {
                logger.error("Failed to execute operation", e);
            }
        }

        @Override
        public boolean isEnabled(int i) {
            return list.getChecked(input[i]);
        }

        @Override
        public Color getColor(int i) {
            return ParticleFXCurveEditorPage.this.getColor(input[i]);
        }
    }

    class CheckStateProvider implements ICheckStateProvider {

        @Override
        public boolean isChecked(Object element) {
            return !hidden.contains(element);
        }

        @Override
        public boolean isGrayed(Object element) {
            return false;
        }
    }

    class ColorLabelProvider extends LabelProvider implements ITableColorProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IPropertyDesc<?, ?>) {
                IPropertyDesc<?, ?> pd = (IPropertyDesc<?, ?>) element;
                return pd.getName();
            }
            return super.getText(element);
        }

        @Override
        public Color getForeground(Object element, int columnIndex) {
            return getColor(element);
        }

        @Override
        public Color getBackground(Object element, int columnIndex) {
            return null;
        }
    }

    @Override
    public void createControl(Composite parent) {

        Display display = parent.getDisplay();
        for (int i = 0; i < colors.length; i++) {
            float hue = 360.0f * i / (colors.length - 1.0f);
            colors[i] = new Color(display, new RGB(hue, 0.85f, 0.7f));
        }

        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 0;
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        list = CheckboxTableViewer.newCheckList(composite, SWT.SINGLE);
        list.setCheckStateProvider(new CheckStateProvider());
        list.addCheckStateListener(this);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 120;
        list.getControl().setLayoutData(gd);
        list.setContentProvider(new ArrayContentProvider());
        list.setLabelProvider(new ColorLabelProvider());

        history = new MergeableDelegatingOperationHistory(PlatformUI.getWorkbench().getOperationSupport().getOperationHistory());
        history.addOperationHistoryListener(this);
        curveEditor = new CurveEditor(composite, SWT.NONE, JFaceResources.getColorRegistry());
        curveEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
        curveEditor.setProvider(new Provider());
    }

    @Override
    public void init(IViewSite site) {
        this.site = site;
        site.getPage().addSelectionListener(this);
        IActionBars actionBars = site.getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), this.undoHandler);
        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), this.redoHandler);
    }

    @Override
    public void dispose() {
        site.getPage().removeSelectionListener(this);
        if (history != null) {
            history.removeOperationHistoryListener(this);
        }

        for (int i = 0; i < colors.length; i++) {
            Color c = colors[i];
            if (c != null) {
                c.dispose();
            }
        }

    }

    @Override
    public Control getControl() {
        return composite;
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
    }

    @Override
    public void setFocus() {
        composite.setFocus();
    }

    @SuppressWarnings({ "unchecked" })
    public void refresh() {
        List<IPropertyDesc<Node, IPropertyObjectWorld>> lst = new ArrayList<IPropertyDesc<Node,IPropertyObjectWorld>>();
        if (selectedNode != null) {
            IPropertyModel<Node, IPropertyObjectWorld> propertyModel = (IPropertyModel<Node, IPropertyObjectWorld>) selectedNode.getAdapter(IPropertyModel.class);
            IPropertyDesc<Node, IPropertyObjectWorld>[] descs = propertyModel.getPropertyDescs();
            for (IPropertyDesc<Node, IPropertyObjectWorld> pd : descs) {
                Object value = propertyModel.getPropertyValue(pd.getId());
                if (value instanceof ValueSpread) {
                    ValueSpread vs = (ValueSpread) value;
                    if (vs.isAnimated()) {
                        lst.add(pd);
                    }
                }
            }
        }

        input = (IPropertyDesc<Node, IPropertyObjectWorld>[]) lst.toArray(new IPropertyDesc<?, ?>[lst.size()]);

        if (!Arrays.equals(input, oldInput)) {
            list.setInput(input);
            curveEditor.setInput(input);
            curveEditor.fit(1.1);
        }

        oldInput = input;
        list.refresh();
        curveEditor.redraw();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        Node newNode = null;

        String id = part.getSite().getId();
        if (!(id.equals("org.eclipse.ui.views.ContentOutline") || part instanceof EditorPart)) {
            // Filter out not interesting selections
            return;
        }

        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            IStructuredSelection structSelect = (IStructuredSelection) selection;
            Object first = structSelect.getFirstElement();
            if (first instanceof Node) {
                newNode = (Node) first;
            }

            IEditorPart editor = site.getPage().getActiveEditor();
            undoContext = (IUndoContext) editor.getAdapter(IUndoContext.class);
        }

        if (newNode != null && undoContext != null) {
            curveEditor.setEnabled(true);
            selectedNode = newNode;
        } else {
            curveEditor.setEnabled(false);
            selectedNode = null;
        }

        refresh();
    }

    @Override
    public void historyNotification(OperationHistoryEvent event) {
        switch (event.getEventType()) {
        case OperationHistoryEvent.DONE:
        case OperationHistoryEvent.UNDONE:
        case OperationHistoryEvent.REDONE:
            if (event.getOperation().hasContext(undoContext)) {
                refresh();
            }
        }
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        Object element = event.getElement();
        if (event.getChecked()) {
            hidden.remove(element);
        } else {
            hidden.add(element);
        }
        this.curveEditor.redraw();
    }

    @Override
    public void frame() {
        this.curveEditor.fit(1.1);
        this.curveEditor.redraw();
    }
}

