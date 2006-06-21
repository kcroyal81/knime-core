/*
 * --------------------------------------------------------------------- *
 *   This source code, its documentation and all appendant files         *
 *   are protected by copyright law. All rights reserved.                *
 *                                                                       *
 *   Copyright, 2003 - 2006                                              *
 *   Universitaet Konstanz, Germany.                                     *
 *   Lehrstuhl fuer Angewandte Informatik                                *
 *   Prof. Dr. Michael R. Berthold                                       *
 *                                                                       *
 *   You may not modify, publish, transmit, transfer or sell, reproduce, *
 *   create derivative works from, distribute, perform, display, or in   *
 *   any way exploit any of the content, in whole or in part, except as  *
 *   otherwise expressly permitted in writing by the copyright owner.    *
 * --------------------------------------------------------------------- *
 */
package de.unikn.knime.core.data.util;

import junit.framework.TestCase;
import de.unikn.knime.core.data.DataCell;
import de.unikn.knime.core.data.DoubleValue;
import de.unikn.knime.core.data.IntValue;
import de.unikn.knime.core.data.StringValue;

/**
 * Unit test for <code>ObjectToDataCellConverter</code>.
 * @author bernd, University of Konstanz
 */
public class ObjectToDataCellConverterTest extends TestCase {
    
    /** Instance to test on. */
    private final ObjectToDataCellConverter m_converter = 
        new ObjectToDataCellConverter();
    
    /** Main class to start unit test from commandline.
     * @param args ignored
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(ObjectToDataCellConverterTest.class);
    }

    /**
     * Class under test for DataCell createDataCell(Object).
     */
    public final void testCreateDataCellObject() {
        int i = 5;
        DataCell c = m_converter.createDataCell(new Integer(i));
        assert ((IntValue)c).getIntValue() == i;
        c = m_converter.createDataCell(new Byte((byte)i));
        assert ((IntValue)c).getIntValue() == i;
        float d = 8.0f;
        c = m_converter.createDataCell(new Double(d));
        assert ((DoubleValue)c).getDoubleValue() == d;
        c = m_converter.createDataCell(new Float(d));
        assert ((DoubleValue)c).getDoubleValue() == d;
        String s = "Test String";
        c = m_converter.createDataCell(s);
        assert ((StringValue)c).getStringValue().equals(s);
        c = m_converter.createDataCell(null);
        assert c.isMissing();
        try {
            c = m_converter.createDataCell(new Object());
            assert false;
        } catch (IllegalArgumentException iae) {
            assert true; // avoid complaints by checkstyle!
        }
    } // testCreateDataCellObject()

    /**
     * Class under test for DataCell createDataCell(double).
     */
    public final void testCreateDataCelldouble() {
        double d = 8.0;
        DataCell c = m_converter.createDataCell(d);
        assert ((DoubleValue)c).getDoubleValue() == d;
    } // testCreateDataCelldouble()

    /**
     * Class under test for DataCell createDataCell(float).
     */
    public final void testCreateDataCellfloat() {
        float f = 8.0f;
        DataCell c = m_converter.createDataCell(f);
        assert ((DoubleValue)c).getDoubleValue() == f;
    } // testCreateDataCellfloat()

    /**
     * Class under test for DataCell createDataCell(int).
     */
    public final void testCreateDataCellint() {
        int i = 12;
        DataCell c = m_converter.createDataCell(i);
        assert ((IntValue)c).getIntValue() == i;
    } // testCreateDataCellint()

    /**
     * Class under test for DataCell createDataCell(byte).
     */
    public final void testCreateDataCellbyte() {
        byte b = 13;
        DataCell c = m_converter.createDataCell(b);
        assert ((IntValue)c).getIntValue() == b;
    } // testCreateDataCellbyte()

    /**
     * Class under test for DataCell createDataCell(boolean).
     */
    public final void testCreateDataCellboolean() {
        boolean b = true;
        DataCell c = m_converter.createDataCell(b);
        assert ((IntValue)c).getIntValue() == 1;
        b = false;
        c = m_converter.createDataCell(b);
        assert ((IntValue)c).getIntValue() == 0;
    } // testCreateDataCellboolean()

}
