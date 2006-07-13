/* 
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
 *   24.10.2005 (gabriel): created
 */
package de.unikn.knime.core.node;

import java.io.IOException;
import java.io.InputStream;

import de.unikn.knime.core.node.config.Config;


/**
 * This ModelContent is used to stored models. This class needs to be
 * modified to provide more functions to store model parts PMML like.
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
public final class ModelContent extends Config 
        implements ModelContentRO, ModelContentWO {
    
    /**
     * Hides public default constructor.
     * 
     * @param key The key for this ModelContent.
     */
    public ModelContent(final String key) {
        super(key);
    }

    /**
     * @see de.unikn.knime.core.node.config.Config#getInstance(java.lang.String)
     */
    @Override
    public Config getInstance(final String key) {
        return new ModelContent(key);
    }

    /**
     * Reads <code>ModelContent</code> settings from the given XML stream 
     * and returns a new <code>ModelContent</code> object.
     * 
     * @param in XML input stream to read settings from.
     * @return A new settings object.
     * @throws IOException If the stream could not be read.
     * @throws NullPointerException If one of the arguments is 
     *         <code>null</code>.
     */
    public static synchronized ModelContentRO loadFromXML(
            final InputStream in) throws IOException {
        ModelContent tmpSettings = new ModelContent("ignored");
        return (ModelContent) Config.loadFromXML(tmpSettings, in);
    }
    
    public void addModelContent(final ModelContent modelContent) {
        super.addConfig(modelContent);
    }

    public ModelContentWO addModelContent(final String key) {
        return (ModelContent) super.addConfig(key);
    }
    
    /**
     * @see Config#getNodeSettings(java.lang.String)
     */
    public ModelContent getModelContent(final String key)
            throws InvalidSettingsException {
        return (ModelContent) super.getConfig(key);
    } 

}
