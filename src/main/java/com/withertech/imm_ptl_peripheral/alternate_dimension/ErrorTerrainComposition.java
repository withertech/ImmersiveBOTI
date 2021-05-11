package com.withertech.imm_ptl_peripheral.alternate_dimension;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.FastRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import java.util.Random;

public class ErrorTerrainComposition {
    
    
    public static final BlockState air = Blocks.AIR.getDefaultState();
    public static final BlockState stone = Blocks.STONE.getDefaultState();
    public static final BlockState water = Blocks.WATER.getDefaultState();
    
    public static final RegionErrorTerrainGenerator.Composition mountain = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        if (funcValue > middle) {
            if (funcValue > middle * middle) {
                return air;
            }
            else {
                return stone;
            }
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition classicalSolid = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint = getSplitPointClassical(worldY, middle, 32.0);
        
        if (funcValue > splitPoint) {
            return stone;
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition classicalHollow = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint = getSplitPointClassical(worldY, middle, 32.0);
        
        if (funcValue > splitPoint) {
            if (funcValue > splitPoint + 2) {
                return air;
            }
            else {
                return stone;
            }
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition classicalWatery = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint = getSplitPointClassical(worldY, middle, 16.0);
        
        if (funcValue > splitPoint) {
            if (((int) funcValue) % 23 == 0) {
                return water;
            }
            else {
                return stone;
            }
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition newSolid = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint1 = Math.abs(middle);
        splitPoint1 *= Math.exp(Math.abs(worldY - ErrorTerrainGenerator.averageY) / 32.0 - 1);
        
        splitPoint1 *= Math.max(
            1.0,
            100.0 / Math.max(1, Math.min(worldY, ErrorTerrainGenerator.maxY - worldY))
        );
        double splitPoint = splitPoint1;
        
        if (funcValue > splitPoint) {
            return stone;
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition floatingSea = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint = getSplitPointClassical(worldY, middle, 16.0);
        
        if (funcValue > splitPoint) {
            if (worldY > 63) {
                return water;
            }
            else {
                return stone;
            }
        }
        else {
            return air;
        }
    };
    
    public static final RegionErrorTerrainGenerator.Composition treasured = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double upPoint = Math.max(middle, Math.max(upMiddle, downMiddle));
        double splitPoint = MathHelper.lerp(
            Math.abs(worldY - 32) / 32.0,
            middle,
            worldY > 32 ? upMiddle : downMiddle
        );
        if (funcValue > splitPoint) {
            if (((int) funcValue) % 37 == 0) {
                Block randomBlock = Registry.BLOCK.getRandom(new Random(
                    FastRandom.mix(worldX, FastRandom.mix(worldY, worldZ))
                ));
                //player should not get beacon so easily
                if (randomBlock == Blocks.BEACON) {
                    randomBlock = Blocks.AIR;
                }
                return randomBlock.getDefaultState();
            }
        }
        return air;
    };
    
    public static final RegionErrorTerrainGenerator.Composition layeredHollow = (
        worldY, funcValue, middle, upMiddle, downMiddle, worldX, worldZ
    ) -> {
        double splitPoint = getSplitPointClassical(worldY, middle, 32.0);
        
        
        if (funcValue > splitPoint) {
            double v = (funcValue - splitPoint) / 4;
            
            if (v > Math.floor(v) + 0.5) {
                return air;
            }
            else {
                return stone;
            }
        }
        else {
            return air;
        }
    };
    
    private static double getSplitPointClassical(int worldY, double middle, double unitFactor) {
        double splitPoint = middle;
        splitPoint *= Math.exp(Math.abs(worldY - ErrorTerrainGenerator.averageY) / unitFactor);
        
        splitPoint *= Math.max(
            0.7,
            30.0 / Math.max(1, Math.min(worldY, ErrorTerrainGenerator.maxY - worldY))
        );
        return splitPoint;
    }
    
    public static final RandomSelector<RegionErrorTerrainGenerator.Composition> selector =
        new RandomSelector.Builder<RegionErrorTerrainGenerator.Composition>()
            .add(25, mountain)
            .add(40, classicalSolid)
            .add(50, classicalHollow)
            .add(10, classicalWatery)
            .add(10, newSolid)
            .add(30, floatingSea)
            .add(3, treasured)
            .add(15, layeredHollow)
            .build();
}
