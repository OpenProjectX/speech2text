/*
 *  This file is part of the noOp organization .
 *
 *  (c) Cyrille Lebeaupin <clebeaupin@noop.fr>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 *
 */

package fr.noop.subtitle.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by clebeaupin on 08/10/15.
 */
public class SubtitleRegionTest {
    private SubtitleRegion tested = new SubtitleRegion(10, 20, 80, 10);

    @Test
    public void testGetX() throws Exception {
        assertEquals(1000, (int) (tested.getX() * 100));
    }

    @Test
    public void testSetX() throws Exception {
        tested.setX(12.5f);
        assertEquals(1250, (int) (tested.getX() * 100));
    }

    @Test
    public void testSetXException1() {
        Assertions.assertThrows(InvalidParameterException.class, () -> tested.setX(-1));

    }

    @Test
    public void testSetXException2() {
        Assertions.assertThrows(InvalidParameterException.class, () -> tested.setX(101));


    }

    @Test
    public void testGetY() throws Exception {
        assertEquals(2000, (int) tested.getY() * 100);
    }

    @Test
    public void testSetY() throws Exception {
        tested.setY(22.5f);
        assertEquals(2250, (int) (tested.getY() * 100));
    }

    @Test
    public void testSetYException1() throws Exception {
        Assertions.assertThrows(InvalidParameterException.class, () -> tested.setX(-1));

        tested.setX(-1);
    }

    @Test
    public void testSetYException2() throws Exception {
        Assertions.assertThrows(InvalidParameterException.class, () -> tested.setX(101));

    }
}