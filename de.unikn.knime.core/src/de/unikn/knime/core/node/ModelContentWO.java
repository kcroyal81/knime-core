/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   12.07.2006 (gabriel): created
 */
package de.unikn.knime.core.node;

import de.unikn.knime.core.node.config.ConfigWO;

/**
 * Write-only <code>ModelContentWO</code> interface.
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
public interface ModelContentWO extends ConfigWO {

    /**
     * Creates new <code>ModelContentWO</code> object for the given key and
     * returns it.
     * @param key The identifier for the given config.
     * @return A new <code>ModelContentWO</code> object.
     */
    ModelContentWO addModelContent(String key);
    
    /**
     * Add the given <code>ModelContent</code> object to this Config using the 
     * key of the argument's <code>ModelContent</code>.
     * @param modelContent The object to add to this <code>Config</code>.
     */
    void addModelContent(ModelContent modelContent);
    
}
