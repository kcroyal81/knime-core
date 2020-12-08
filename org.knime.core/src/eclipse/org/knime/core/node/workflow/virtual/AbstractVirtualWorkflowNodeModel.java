/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Dec 8, 2020 (hornm): created
 */
package org.knime.core.node.workflow.virtual;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.exec.dataexchange.PortObjectRepository;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.NativeNodeContainer;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.node.workflow.virtual.parchunk.FlowVirtualScopeContext;

/**
 * TODO A node that virtually executes another workflow which is created within this very same workflow and deleted
 * after successful execution.
 *
 * @author Martin Horn, KNIME GmbH, Konstanz, Germany
 * @since 4.3
 */
public abstract class AbstractVirtualWorkflowNodeModel extends NodeModel implements PortObjectHolder {

    private static final String CFG_PORT_OBJECT_IDS = "port_object_ids";

    private static final String INTERNALS_FILE_PORT_OBJECT_IDS = "port_object_ids.xml.gz";

    private List<UUID> m_portObjectIds;

    private List<PortObject> m_portObjects;

    /**
     * @param inPortTypes
     * @param outPortTypes
     */
    protected AbstractVirtualWorkflowNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }

    @Override
    protected void reset() {
        if (m_portObjectIds != null) {
            m_portObjectIds.forEach(PortObjectRepository::remove);
            m_portObjects.forEach(PortObjectRepository::removeIDFor);
            m_portObjectIds = null;
            m_portObjects = null;
        }
    }

    /**
     * TODO
     *
     * @param exec
     * @param virtualInNode
     */
    public void prepareVirtualWorkflowExecution(final ExecutionContext exec, final NativeNodeContainer virtualInNode) {
        FlowVirtualScopeContext virtualScope =
            virtualInNode.getOutgoingFlowObjectStack().peek(FlowVirtualScopeContext.class);

        CheckUtils.checkNotNull("The node '" + virtualInNode.getNameWithID()
            + "' doesn't provide a virtual scope context. Most likely an implementation error.");

        // Sets the port object id call back on the virtual scope.
        // The call back is triggered (possibly multiple times) during the execution of this workflow (fragment),
        // e.g., if there is a 'Capture Workflow End' node whose scope has 'static' input directly connected into
        // the scope. Those 'static inputs' are made available via this call back (via the the PortObjectRepository)
        // such that this node can, retrieve, persist and later restore them for downstream nodes (that make use of
        // the potentially output workflow port object by this workflow execution, such as the Workflow Writer).
        virtualScope.setPortObjectIDCallback(fct -> {
            UUID id = fct.apply(exec);
            m_portObjectIds.add(id);
            m_portObjects.add(PortObjectRepository.get(id).get());
        });

        // set the file store handler to use in the virtual scope:
        // all contained nodes need to use the file store handler of this workflow executor node
        NodeContainer nc = NodeContext.getContext().getNodeContainer();
        CheckUtils.checkArgumentNotNull(nc, "Not a local workflow");
        virtualScope.setNodeContainer((NativeNodeContainer)nc);

        m_portObjectIds = new ArrayList<>();
        m_portObjects = new ArrayList<>();
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        File f = new File(nodeInternDir, INTERNALS_FILE_PORT_OBJECT_IDS);
        if (f.exists()) {
            try (InputStream in = new GZIPInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                try {
                    NodeSettingsRO settings = NodeSettings.loadFromXML(in);
                    if (settings.containsKey(CFG_PORT_OBJECT_IDS)) {
                        m_portObjectIds = Arrays.stream(settings.getStringArray(CFG_PORT_OBJECT_IDS))
                            .map(UUID::fromString).collect(Collectors.toList());
                        addToPortObjectRepository();
                    }
                } catch (InvalidSettingsException ise) {
                    throw new IOException("Unable to read port object ids", ise);
                }
            }
        }
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        if (m_portObjectIds != null) {
            NodeSettings settings = new NodeSettings("port_object_ids");
            String[] ids =
                m_portObjectIds.stream().map(UUID::toString).toArray(i -> new String[m_portObjectIds.size()]);
            settings.addStringArray(CFG_PORT_OBJECT_IDS, ids);
            try (GZIPOutputStream gzs = new GZIPOutputStream(new BufferedOutputStream(
                new FileOutputStream(new File(nodeInternDir, INTERNALS_FILE_PORT_OBJECT_IDS))))) {
                settings.saveToXML(gzs);
            }
        }
    }

    @Override
    public void setInternalPortObjects(final PortObject[] portObjects) {
        m_portObjects = Arrays.asList(portObjects);
        addToPortObjectRepository();
    }

    private void addToPortObjectRepository() {
        if (m_portObjects != null && m_portObjectIds != null) {
            assert m_portObjects.size() == m_portObjectIds.size();
            for (int i = 0; i < m_portObjects.size(); i++) {
                PortObjectRepository.add(m_portObjectIds.get(i), m_portObjects.get(i));
            }
        }
    }

    @Override
    public PortObject[] getInternalPortObjects() {
        return m_portObjects.toArray(new PortObject[m_portObjects.size()]);
    }

}
