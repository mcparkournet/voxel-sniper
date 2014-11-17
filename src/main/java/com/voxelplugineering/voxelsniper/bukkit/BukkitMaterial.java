/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 The Voxel Plugineering Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.voxelplugineering.voxelsniper.bukkit;

import org.bukkit.Material;

import com.voxelplugineering.voxelsniper.common.CommonMaterial;

public class BukkitMaterial extends CommonMaterial<Material>
{

    public BukkitMaterial(Material value)
    {
        super(value);
    }

    @Override
    public String toString()
    {
        return this.getValue().name();
    }

    @Override
    public boolean isBlock()
    {
        return this.getValue().isBlock();
    }

    @Override
    public boolean hasGravity()
    {
        return this.getValue().hasGravity();
    }

    @Override
    public boolean isSolid()
    {
        return this.getValue().isSolid();
    }

    @Override
    public boolean isTransparent()
    {
        return this.getValue().isTransparent();
    }

}
