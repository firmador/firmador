/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestGUISelector {
    private final GUISelector guiSelector = new GUISelector();

    @Test
    void testGetGUIClassNameWithArgsWithoutD(){
        String[] args = {"-ttest"};
        String result = this.guiSelector.getGUIClassName(args);

        assertFalse(result.isEmpty());
        assertEquals("swing", result);
    }

    @Test
    void testGetGUIClassNameWithArgsWithD(){
        String[] args = {"-dtest"};
        String result = this.guiSelector.getGUIClassName(args);

        assertFalse(result.isEmpty());
        assertEquals("test", result);
    }

    @Test
    void testGetInterfaceWithNameParamAsArgs(){
        GUIInterface guiInterface = this.guiSelector.getInterface("args");

        assertNotNull(guiInterface);
        assertInstanceOf(GUIArgs.class, guiInterface);
    }

    @Test
    void testGetInterfaceWithNameParamAsShell(){
        GUIInterface guiInterface = this.guiSelector.getInterface("shell");

        assertNotNull(guiInterface);
        assertInstanceOf(GUIShell.class, guiInterface);
    }

    @Test
    void testGetInterfaceWithNameParamAsSomethingElse(){
        GUIInterface guiInterface = this.guiSelector.getInterface("test");

        assertNotNull(guiInterface);
        assertInstanceOf(GUISwing.class, guiInterface);
    }

    @Test
    void testGetInterfaceWithArgsParam(){
        String[] args = {"-dshell"};
        GUIInterface guiInterface = this.guiSelector.getInterface(args);

        assertNotNull(guiInterface);
        assertInstanceOf(GUIShell.class, guiInterface);
    }
}
