/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.alkemy.util.Node;
import org.alkemy.util.Nodes;
import org.alkemy.util.Node.Builder;
import org.junit.Test;

@SuppressWarnings("unused")
public class NodesTest
{
    @Test
    public void testDepth1()
    {
        final Builder<Integer> r = Nodes.arborescence(3);

        // r -> { r1, r2, r3 }
        final Builder<Integer> r1 = r.addChild(1);
        final Builder<Integer> r2 = r.addChild(1);
        final Builder<Integer> r3 = r.addChild(2);

        final Node<Integer> c = r.build();
        final Node<Integer> c1 = c.children().get(0);
        final Node<Integer> c2 = c.children().get(1);
        final Node<Integer> c3 = c.children().get(2);

        assertThat(c.branchDepth(), is(1));
        assertThat(c1.branchDepth(), is(0));
        assertThat(c2.branchDepth(), is(0));
        assertThat(c3.branchDepth(), is(0));
    }

    @Test
    public void testDepthLine()
    {
        // r -> { r1 } -> { r11 } -> { r111 } -> { r1111 } -> { r11111 }
        final Builder<Integer> r = Nodes.arborescence(3);
        final Builder<Integer> r1 = r.addChild(1);
        final Builder<Integer> r11 = r1.addChild(1);
        final Builder<Integer> r111 = r11.addChild(1);
        final Builder<Integer> r1111 = r111.addChild(1);
        final Builder<Integer> r11111 = r1111.addChild(1);

        final Node<Integer> c = r.build();
        final Node<Integer> c1 = c.children().get(0);
        final Node<Integer> c11 = c1.children().get(0);
        final Node<Integer> c111 = c11.children().get(0);
        final Node<Integer> c1111 = c111.children().get(0);
        final Node<Integer> c11111 = c1111.children().get(0);

        assertThat(c.branchDepth(), is(5));
        assertThat(c1.branchDepth(), is(4));
        assertThat(c11.branchDepth(), is(3));
        assertThat(c111.branchDepth(), is(2));
        assertThat(c1111.branchDepth(), is(1));
        assertThat(c11111.branchDepth(), is(0));
    }

    @Test
    public void testDepthRightMost()
    {
        final Builder<Integer> r = Nodes.arborescence(3);

        // r -> { r1, r2, r3 } depth(r) = Max(depth(r1), depth(r2), depth(r3)) + 1
        final Builder<Integer> r1 = r.addChild(1);
        final Builder<Integer> r2 = r.addChild(1);
        final Builder<Integer> r3 = r.addChild(2);

        // r1 -> { r11 } depth : 1
        final Builder<Integer> r11 = r1.addChild(1);

        // r2 -> { r21, r22, r23 } depth : 1
        final Builder<Integer> r21 = r2.addChild(1);
        final Builder<Integer> r22 = r2.addChild(1);
        final Builder<Integer> r23 = r2.addChild(1);

        // r3 -> { r31 } depth : 3
        final Builder<Integer> r31 = r3.addChild(2);

        // r31 -> { r311 } depth : 2
        final Builder<Integer> r311 = r31.addChild(2);

        // r311 -> { r3111} depth : 1
        final Builder<Integer> r3111 = r311.addChild(2);

        final Node<Integer> c = r.build();
        final Node<Integer> c1 = c.children().get(0);
        final Node<Integer> c11 = c1.children().get(0);
        final Node<Integer> c2 = c.children().get(1);
        final Node<Integer> c21 = c2.children().get(0);
        final Node<Integer> c22 = c2.children().get(1);
        final Node<Integer> c23 = c2.children().get(2);
        final Node<Integer> c3 = c.children().get(2);
        final Node<Integer> c31 = c3.children().get(0);
        final Node<Integer> c311 = c31.children().get(0);
        final Node<Integer> c3111 = c311.children().get(0);

        assertThat(c.branchDepth(), is(4));
        assertThat(c1.branchDepth(), is(1));
        assertThat(c11.branchDepth(), is(0));
        assertThat(c2.branchDepth(), is(1));
        assertThat(c21.branchDepth(), is(0));
        assertThat(c22.branchDepth(), is(0));
        assertThat(c23.branchDepth(), is(0));
        assertThat(c3.branchDepth(), is(3));
        assertThat(c31.branchDepth(), is(2));
        assertThat(c311.branchDepth(), is(1));
        assertThat(c3111.branchDepth(), is(0));
    }

    @Test
    public void testDepthMidMost()
    {
        final Builder<Integer> r = Nodes.arborescence(3);

        // r -> { r1, r2, r3 } depth(r) = Max(depth(r1), depth(r2), depth(r3)) + 1
        final Builder<Integer> r1 = r.addChild(1);
        final Builder<Integer> r2 = r.addChild(1);
        final Builder<Integer> r3 = r.addChild(2);

        // r1 -> { r11 } depth : 1
        final Builder<Integer> r11 = r1.addChild(1);

        // r3 -> { r31 } depth : 3
        final Builder<Integer> r21 = r2.addChild(2);

        // r31 -> { r311 } depth : 2
        final Builder<Integer> r211 = r21.addChild(2);

        // r311 -> { r3111} depth : 1
        final Builder<Integer> r2111 = r211.addChild(2);
        
        // r2 -> { r21, r22, r23 } depth : 1
        final Builder<Integer> r31 = r3.addChild(1);
        final Builder<Integer> r32 = r3.addChild(1);
        final Builder<Integer> r33 = r3.addChild(1);

        final Node<Integer> c = r.build();
        final Node<Integer> c1 = c.children().get(0);
        final Node<Integer> c11 = c1.children().get(0);
        final Node<Integer> c2 = c.children().get(1);
        final Node<Integer> c21 = c2.children().get(0);
        final Node<Integer> c211 = c21.children().get(0);
        final Node<Integer> c2111 = c211.children().get(0);
        final Node<Integer> c3 = c.children().get(2);
        final Node<Integer> c31 = c3.children().get(0);
        final Node<Integer> c32 = c3.children().get(1);
        final Node<Integer> c33 = c3.children().get(2);

        assertThat(c.branchDepth(), is(4));
        assertThat(c1.branchDepth(), is(1));
        assertThat(c11.branchDepth(), is(0));
        assertThat(c2.branchDepth(), is(3));
        assertThat(c21.branchDepth(), is(2));
        assertThat(c211.branchDepth(), is(1));
        assertThat(c2111.branchDepth(), is(0));
        assertThat(c3.branchDepth(), is(1));
        assertThat(c31.branchDepth(), is(0));
        assertThat(c32.branchDepth(), is(0));
        assertThat(c33.branchDepth(), is(0));
    }

    @Test
    public void testDepthLeftMost()
    {
        final Builder<Integer> r = Nodes.arborescence(3);

        // r -> { r1, r2, r3 } depth(r) = Max(depth(r1), depth(r2), depth(r3)) + 1
        final Builder<Integer> r1 = r.addChild(1);
        final Builder<Integer> r2 = r.addChild(1);
        final Builder<Integer> r3 = r.addChild(2);

        // r1 -> { r11 } depth : 3
        final Builder<Integer> r11 = r1.addChild(2);

        // r11 -> { r111 } depth : 2
        final Builder<Integer> r111 = r11.addChild(2);

        // r111 -> { r1111} depth : 1
        final Builder<Integer> r1111 = r111.addChild(2);
        
        // r2 -> { r21, r22, r23 } depth : 1
        final Builder<Integer> r21 = r2.addChild(1);
        final Builder<Integer> r22 = r2.addChild(1);
        final Builder<Integer> r23 = r2.addChild(1);
        
        // r3 -> { r31 } depth : 1
        final Builder<Integer> r31 = r3.addChild(1);

        final Node<Integer> c = r.build();
        final Node<Integer> c1 = c.children().get(0);
        final Node<Integer> c11 = c1.children().get(0);
        final Node<Integer> c111 = c11.children().get(0);
        final Node<Integer> c1111 = c111.children().get(0);
        final Node<Integer> c2 = c.children().get(1);
        final Node<Integer> c21 = c2.children().get(0);
        final Node<Integer> c22 = c2.children().get(1);
        final Node<Integer> c23 = c2.children().get(2);
        final Node<Integer> c3 = c.children().get(2);
        final Node<Integer> c31 = c3.children().get(0);

        assertThat(c.branchDepth(), is(4));
        assertThat(c3.branchDepth(), is(1));
        assertThat(c31.branchDepth(), is(0));
        assertThat(c2.branchDepth(), is(1));
        assertThat(c21.branchDepth(), is(0));
        assertThat(c22.branchDepth(), is(0));
        assertThat(c23.branchDepth(), is(0));
        assertThat(c1.branchDepth(), is(3));
        assertThat(c11.branchDepth(), is(2));
        assertThat(c111.branchDepth(), is(1));
        assertThat(c1111.branchDepth(), is(0));
    }
}
