package com.withertech.imm_boti;

import com.google.common.collect.Streams;
import com.withertech.imm_boti.block_manipulation.BlockManipulationClient;
import com.withertech.imm_boti.ducks.IERayTraceContext;
import com.withertech.imm_boti.ducks.IEWorldChunk;
import com.withertech.imm_boti.my_util.IntBox;
import com.withertech.imm_boti.portal.Portal;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// helper methods
public class Helper {
    
    private static final Logger LOGGER = LogManager.getLogger("Portal");
    
    public static FloatBuffer getModelViewMatrix() {
        return getMatrix(GL11.GL_MODELVIEW_MATRIX);
    }
    
    public static FloatBuffer getProjectionMatrix() {
        return getMatrix(GL11.GL_PROJECTION_MATRIX);
    }
    
    public static FloatBuffer getTextureMatrix() {
        return getMatrix(GL11.GL_TEXTURE_MATRIX);
    }
    
    public static FloatBuffer getMatrix(int matrixId) {
        FloatBuffer temp = BufferUtils.createFloatBuffer(16);
        
        GL11.glGetFloatv(matrixId, temp);
        
        return temp;
    }
    
    //get the intersect point of a line and a plane
    //a line: p = lineCenter + t * lineDirection
    //get the t of the colliding point
    //normal and lineDirection have to be normalized
    public static double getCollidingT(
        Vector3d planeCenter,
        Vector3d planeNormal,
        Vector3d lineCenter,
        Vector3d lineDirection
    ) {
        return (planeCenter.subtract(lineCenter).dotProduct(planeNormal))
            /
            (lineDirection.dotProduct(planeNormal));
    }
    
    public static boolean isInFrontOfPlane(
        Vector3d pos,
        Vector3d planePos,
        Vector3d planeNormal
    ) {
        return pos.subtract(planePos).dotProduct(planeNormal) > 0;
    }
    
    public static Vector3d fallPointOntoPlane(
        Vector3d point,
        Vector3d planePos,
        Vector3d planeNormal
    ) {
        double t = getCollidingT(planePos, planeNormal, point, planeNormal);
        return point.add(planeNormal.scale(t));
    }
    
    public static Vector3i getUnitFromAxis(Direction.Axis axis) {
        return Direction.getFacingFromAxis(
            Direction.AxisDirection.POSITIVE,
            axis
        ).getDirectionVec();
    }
    
    public static int getCoordinate(Vector3i v, Direction.Axis axis) {
        return axis.getCoordinate(v.getX(), v.getY(), v.getZ());
    }
    
    public static double getCoordinate(Vector3d v, Direction.Axis axis) {
        return axis.getCoordinate(v.x, v.y, v.z);
    }
    
    public static int getCoordinate(Vector3i v, Direction direction) {
        return getCoordinate(v, direction.getAxis()) *
            (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1);
    }
    
    public static Vector3d putCoordinate(Vector3d v, Direction.Axis axis, double value) {
        if (axis == Direction.Axis.X) {
            return new Vector3d(value, v.y, v.z);
        }
        else if (axis == Direction.Axis.Y) {
            return new Vector3d(v.x, value, v.z);
        }
        else {
            return new Vector3d(v.x, v.y, value);
        }
    }
    
    public static <A, B> Tuple<B, A> swaped(Tuple<A, B> p) {
        return new Tuple<>(p.getB(), p.getA());
    }
    
    public static <T> T uniqueOfThree(T a, T b, T c) {
        if (a.equals(b)) {
            return c;
        }
        else if (b.equals(c)) {
            return a;
        }
        else {
            assert a.equals(c);
            return b;
        }
    }
    
    public static BlockPos max(BlockPos a, BlockPos b) {
        return new BlockPos(
            Math.max(a.getX(), b.getX()),
            Math.max(a.getY(), b.getY()),
            Math.max(a.getZ(), b.getZ())
        );
    }
    
    public static BlockPos min(BlockPos a, BlockPos b) {
        return new BlockPos(
            Math.min(a.getX(), b.getX()),
            Math.min(a.getY(), b.getY()),
            Math.min(a.getZ(), b.getZ())
        );
    }
    
    public static Tuple<Direction.Axis, Direction.Axis> getAnotherTwoAxis(Direction.Axis axis) {
        switch (axis) {
            case X:
                return new Tuple<>(Direction.Axis.Y, Direction.Axis.Z);
            case Y:
                return new Tuple<>(Direction.Axis.Z, Direction.Axis.X);
            case Z:
                return new Tuple<>(Direction.Axis.X, Direction.Axis.Y);
        }
        throw new IllegalArgumentException();
    }
    
    public static BlockPos scale(Vector3i v, int m) {
        return new BlockPos(v.getX() * m, v.getY() * m, v.getZ() * m);
    }
    
    public static BlockPos divide(Vector3i v, int d) {
        return new BlockPos(v.getX() / d, v.getY() / d, v.getZ() / d);
    }
    
    public static Direction[] getAnotherFourDirections(Direction.Axis axisOfNormal) {
        Tuple<Direction.Axis, Direction.Axis> anotherTwoAxis = getAnotherTwoAxis(
            axisOfNormal
        );
        return new Direction[]{
            Direction.getFacingFromAxis(
                Direction.AxisDirection.POSITIVE, anotherTwoAxis.getA()
            ),
            Direction.getFacingFromAxis(
                Direction.AxisDirection.POSITIVE, anotherTwoAxis.getB()
            ),
            Direction.getFacingFromAxis(
                Direction.AxisDirection.NEGATIVE, anotherTwoAxis.getA()
            ),
            Direction.getFacingFromAxis(
                Direction.AxisDirection.NEGATIVE, anotherTwoAxis.getB()
            )
        };
    }
    
    @Deprecated
    public static Tuple<Direction.Axis, Direction.Axis> getPerpendicularAxis(Direction facing) {
        Tuple<Direction.Axis, Direction.Axis> axises = getAnotherTwoAxis(facing.getAxis());
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            axises = new Tuple<>(axises.getB(), axises.getA());
        }
        return axises;
    }
    
    public static Tuple<Direction, Direction> getPerpendicularDirections(Direction facing) {
        Tuple<Direction.Axis, Direction.Axis> axises = getAnotherTwoAxis(facing.getAxis());
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            axises = new Tuple<>(axises.getB(), axises.getA());
        }
        return new Tuple<>(
            Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axises.getA()),
            Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axises.getB())
        );
    }
    
    public static Vector3d getBoxSize(AxisAlignedBB box) {
        return new Vector3d(box.getXSize(), box.getYSize(), box.getZSize());
    }
    
    public static AxisAlignedBB getBoxSurfaceInversed(AxisAlignedBB box, Direction direction) {
        double size = getCoordinate(getBoxSize(box), direction.getAxis());
        Vector3d shrinkVec = Vector3d.copy(direction.getDirectionVec()).scale(size);
        return box.contract(shrinkVec.x, shrinkVec.y, shrinkVec.z);
    }
    
    public static AxisAlignedBB getBoxSurface(AxisAlignedBB box, Direction direction) {
        return getBoxSurfaceInversed(box, direction.getOpposite());
    }
    
    public static IntBox expandRectangle(
        BlockPos startingPos,
        Predicate<BlockPos> blockPosPredicate, Direction.Axis axis
    ) {
        IntBox wallArea = new IntBox(startingPos, startingPos);
        
        for (Direction direction : getAnotherFourDirections(axis)) {
            
            wallArea = expandArea(
                wallArea, blockPosPredicate, direction
            );
        }
        return wallArea;
    }
    
    public static IntBox expandBoxArea(
        BlockPos startingPos,
        Predicate<BlockPos> blockPosPredicate
    ) {
        IntBox box = new IntBox(startingPos, startingPos);
        
        for (Direction direction : Direction.values()) {
            box = expandArea(
                box, blockPosPredicate, direction
            );
        }
        return box;
    }
    
    public static int getChebyshevDistance(
        int x1, int z1,
        int x2, int z2
    ) {
        return Math.max(
            Math.abs(x1 - x2),
            Math.abs(z1 - z2)
        );
    }
    
    public static AxisAlignedBB getBoxByBottomPosAndSize(Vector3d boxBottomCenter, Vector3d viewBoxSize) {
        return new AxisAlignedBB(
            boxBottomCenter.subtract(viewBoxSize.x / 2, 0, viewBoxSize.z / 2),
            boxBottomCenter.add(viewBoxSize.x / 2, viewBoxSize.y, viewBoxSize.z / 2)
        );
    }
    
    public static Vector3d getBoxBottomCenter(AxisAlignedBB box) {
        return new Vector3d(
            (box.maxX + box.minX) / 2,
            box.minY,
            (box.maxZ + box.minZ) / 2
        );
    }
    
    public static double getDistanceToRectangle(
        double pointX, double pointY,
        double rectAX, double rectAY,
        double rectBX, double rectBY
    ) {
        assert rectAX <= rectBX;
        assert rectAY <= rectBY;
        
        double wx1 = rectAX - pointX;
        double wx2 = rectBX - pointX;
        double dx = (wx1 * wx2 < 0 ? 0 : Math.min(Math.abs(wx1), Math.abs(wx2)));
        
        double wy1 = rectAY - pointY;
        double wy2 = rectBY - pointY;
        double dy = (wy1 * wy2 < 0 ? 0 : Math.min(Math.abs(wy1), Math.abs(wy2)));
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static class SimpleBox<T> {
        public T obj;
        
        public SimpleBox(T obj) {
            this.obj = obj;
        }
    }
    
    @Nullable
    public static <T> T getLastSatisfying(Stream<T> stream, Predicate<T> predicate) {
        T last = null;
        
        Iterator<T> iterator = stream.iterator();
        
        while (iterator.hasNext()) {
            T obj = iterator.next();
            if (predicate.test(obj)) {
                last = obj;
            }
            else {
                return last;
            }
        }
        
        return last;
    }
    
    public static void doNotEatExceptionMessage(
        Runnable func
    ) {
        try {
            func.run();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public static <T> String myToString(
        Stream<T> stream
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        stream.forEach(obj -> {
            stringBuilder.append(obj.toString());
            stringBuilder.append('\n');
        });
        return stringBuilder.toString();
    }
    
    public static <A, B> Stream<Tuple<A, B>> composeTwoStreamsWithEqualLength(
        Stream<A> a,
        Stream<B> b
    ) {
        Iterator<A> aIterator = a.iterator();
        Iterator<B> bIterator = b.iterator();
        Iterator<Tuple<A, B>> iterator = new Iterator<Tuple<A, B>>() {
            
            @Override
            public boolean hasNext() {
                assert aIterator.hasNext() == bIterator.hasNext();
                return aIterator.hasNext();
            }
            
            @Override
            public Tuple<A, B> next() {
                return new Tuple<>(aIterator.next(), bIterator.next());
            }
        };
        
        return Streams.stream(iterator);
    }
    
    public static void log(Object str) {
        LOGGER.info(str);
    }
    
    public static void err(Object str) {
        LOGGER.error(str);
    }
    
    public static void dbg(Object str) {
        LOGGER.debug(str);
    }
    
    public static Vector3d[] eightVerticesOf(AxisAlignedBB box) {
        return new Vector3d[]{
            new Vector3d(box.minX, box.minY, box.minZ),
            new Vector3d(box.minX, box.minY, box.maxZ),
            new Vector3d(box.minX, box.maxY, box.minZ),
            new Vector3d(box.minX, box.maxY, box.maxZ),
            new Vector3d(box.maxX, box.minY, box.minZ),
            new Vector3d(box.maxX, box.minY, box.maxZ),
            new Vector3d(box.maxX, box.maxY, box.minZ),
            new Vector3d(box.maxX, box.maxY, box.maxZ)
        };
    }
    
    public static void putVec3d(CompoundNBT compoundTag, String name, Vector3d vec3d) {
        compoundTag.putDouble(name + "X", vec3d.x);
        compoundTag.putDouble(name + "Y", vec3d.y);
        compoundTag.putDouble(name + "Z", vec3d.z);
    }
    
    public static Vector3d getVec3d(CompoundNBT compoundTag, String name) {
        return new Vector3d(
            compoundTag.getDouble(name + "X"),
            compoundTag.getDouble(name + "Y"),
            compoundTag.getDouble(name + "Z")
        );
    }
    
    public static void putVec3i(CompoundNBT compoundTag, String name, Vector3i vec3i) {
        compoundTag.putInt(name + "X", vec3i.getX());
        compoundTag.putInt(name + "Y", vec3i.getY());
        compoundTag.putInt(name + "Z", vec3i.getZ());
    }
    
    public static BlockPos getVec3i(CompoundNBT compoundTag, String name) {
        return new BlockPos(
            compoundTag.getInt(name + "X"),
            compoundTag.getInt(name + "Y"),
            compoundTag.getInt(name + "Z")
        );
    }
    
    public static <T> void compareOldAndNew(
        Set<T> oldSet,
        Set<T> newSet,
        Consumer<T> forRemoved,
        Consumer<T> forAdded
    ) {
        oldSet.stream().filter(
            e -> !newSet.contains(e)
        ).forEach(
            forRemoved
        );
        newSet.stream().filter(
            e -> !oldSet.contains(e)
        ).forEach(
            forAdded
        );
    }
    
    public static long secondToNano(double second) {
        return (long) (second * 1000000000L);
    }
    
    public static double nanoToSecond(long nano) {
        return nano / 1000000000.0;
    }
    
    public static IntBox expandArea(
        IntBox originalArea,
        Predicate<BlockPos> predicate,
        Direction direction
    ) {
        IntBox currentBox = originalArea;
        for (int i = 1; i < 42; i++) {
            IntBox expanded = currentBox.getExpanded(direction, 1);
            if (expanded.getSurfaceLayer(direction).stream().allMatch(predicate)) {
                currentBox = expanded;
            }
            else {
                return currentBox;
            }
        }
        return currentBox;
    }
    
    public static <A, B> B reduce(
        B start,
        Stream<A> stream,
        BiFunction<B, A, B> func
    ) {
        return stream.reduce(
            start,
            func,
            (a, b) -> {
                throw new IllegalStateException("combiner should only be used in parallel");
            }
        );
    }
    
    public static <T> T noError(Callable<T> func) {
        try {
            return func.call();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * {@link ObjectList} does not override removeIf() so it's O(n^2)
     * {@link ArrayList#removeIf(Predicate)} uses a bitset to ensure integrity
     * in case of exception thrown but introduces performance overhead
     */
    public static <T> void removeIf(ObjectList<T> list, Predicate<T> predicate) {
        int placingIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            T curr = list.get(i);
            if (!predicate.test(curr)) {
                list.set(placingIndex, curr);
                placingIndex += 1;
            }
        }
        list.removeElements(placingIndex, list.size());
    }
    
    public static <T, S> Stream<S> wrapAdjacentAndMap(
        Stream<T> stream,
        BiFunction<T, T, S> function
    ) {
        Iterator<T> iterator = stream.iterator();
        return Streams.stream(new Iterator<S>() {
            private boolean isBuffered = false;
            private T buffer;
            
            private void fillBuffer() {
                if (!isBuffered) {
                    assert iterator.hasNext();
                    isBuffered = true;
                    buffer = iterator.next();
                }
            }
            
            private T takeBuffer() {
                assert isBuffered;
                isBuffered = false;
                return buffer;
            }
            
            @Override
            public boolean hasNext() {
                if (!iterator.hasNext()) {
                    return false;
                }
                fillBuffer();
                return iterator.hasNext();
            }
            
            @Override
            public S next() {
                fillBuffer();
                T a = takeBuffer();
                fillBuffer();
                return function.apply(a, buffer);
            }
        });
    }
    
    //map and reduce at the same time
    public static <A, B> Stream<B> mapReduce(
        Stream<A> stream,
        BiFunction<B, A, B> func,
        SimpleBox<B> startValue
    ) {
        return stream.map(a -> {
            startValue.obj = func.apply(startValue.obj, a);
            return startValue.obj;
        });
    }
    
    //another implementation using mapReduce but creates more garbage objects
    public static <T, S> Stream<S> wrapAdjacentAndMap1(
        Stream<T> stream,
        BiFunction<T, T, S> function
    ) {
        Iterator<T> iterator = stream.iterator();
        if (!iterator.hasNext()) {
            return Stream.empty();
        }
        T firstValue = iterator.next();
        Stream<T> newStream = Streams.stream(iterator);
        return mapReduce(
            newStream,
            (Tuple<T, S> lastPair, T curr) ->
                new Tuple<T, S>(curr, function.apply(lastPair.getA(), curr)),
            new SimpleBox<>(new Tuple<T, S>(firstValue, null))
        ).map(pair -> pair.getB());
    }
    
    public static <T> T makeIntoExpression(T t, Consumer<T> func) {
        func.accept(t);
        return t;
    }
    
    public static void putUuid(CompoundNBT tag, String key, UUID uuid) {
        tag.putLong(key + "Most", uuid.getMostSignificantBits());
        tag.putLong(key + "Least", uuid.getLeastSignificantBits());
    }
    
    public static UUID getUuid(CompoundNBT tag, String key) {
        return new UUID(tag.getLong(key + "Most"), tag.getLong(key + "Least"));
    }
    
    public static Vector3d getFlippedVec(Vector3d vec, Vector3d flippingAxis) {
        Vector3d component = getProjection(vec, flippingAxis);
        return vec.subtract(component).scale(-1).add(component);
    }
    
    public static Vector3d getProjection(Vector3d vec, Vector3d direction) {
        return direction.scale(vec.dotProduct(direction));
    }
    
    /**
     * Searches nearby chunks to look for a certain sub/class of entity. In the specified {@code world}, the chunk that
     * {@code pos} is in will be used as the center of search. That chunk will be expanded by {@code chunkRadius} chunks
     * in all directions to define the search area. Then, on all Y levels, those chunks will be searched for entities of
     * class {@code entityClass}. Then all entities found will be returned.
     * <p>
     * If you define a {@code chunkRadius} of 1, 9 chunks will be searched. If you define one of 2, then 25 chunks will
     * be searched. This can be an extreme performance bottleneck, so yse it sparingly such as a response to user input.
     *
     * @param world       The world in which to search for entities.
     * @param pos         The chunk that this position is located in will be used as the center of search.
     * @param chunkRadius Integer number of chunks to expand the square search area by.
     * @param entityClass The entity class to search for.
     * @param <T>         The entity class that will be returned in the list.
     * @return All entities in the nearby chunks with type T.
     * @author LoganDark
     */
    @SuppressWarnings("WeakerAccess")
    public static <T extends Entity> List<T> getNearbyEntities(
        World world,
        Vector3d pos,
        int chunkRadius,
        Class<T> entityClass
    ) {
        ArrayList<T> entities = new ArrayList<>();
        int chunkX = (int) pos.x / 16;
        int chunkZ = (int) pos.z / 16;
        
        for (int z = -chunkRadius + 1; z < chunkRadius; z++) {
            for (int x = -chunkRadius + 1; x < chunkRadius; x++) {
                int aX = chunkX + x;
                int aZ = chunkZ + z;
                
                // WorldChunk contains a private variable called entitySections that groups all entities in the chunk by
                // their Y level. Here we are using a Mixin duck typing interface thing to get that private variable and
                // then manually search it. This is faster than using the built-in WorldChunk methods that do not do
                // what we want.
                ClassInheritanceMultiMap<Entity>[] entitySections = ((IEWorldChunk) world.getChunk(
                    aX,
                    aZ
                )).portal_getEntitySections();
                for (ClassInheritanceMultiMap<Entity> entitySection : entitySections) {
                    entities.addAll(entitySection.getByClass(entityClass));
                }
            }
        }
        
        return entities;
    }
    
    /**
     * Returns all portals intersecting the line from start->end.
     *
     * @param world                The world in which to ray trace for portals.
     * @param start                The start of the line defining the ray to trace.
     * @param end                  The end of the line defining the ray to trace.
     * @param includeGlobalPortals Whether or not to include global portals in the ray trace.
     * @param filter               Filter the portals that this function returns. Nullable
     * @return A list of portals and their intersection points with the line, sorted by nearest portals first.
     * @author LoganDark
     */
    @SuppressWarnings("WeakerAccess")
    public static List<Tuple<Portal, Vector3d>> rayTracePortals(
        World world,
        Vector3d start,
        Vector3d end,
        boolean includeGlobalPortals,
        Predicate<Portal> filter
    ) {
        // This will be the center of the chunk search, rather than using start or end. This will allow the radius to be
        // smaller, and as a result, the search to be faster and slightly less inefficient.
        //
        // The searching method employed by getNearbyEntities is still not ideal, but it's the best idea I have.
        Vector3d middle = start.scale(0.5).add(end.scale(0.5));
        
        // This could result in searching more chunks than necessary, but it always expands to completely cover any
        // chunks the line from start->end passes through.
        int chunkRadius = (int) Math.ceil(Math.abs(start.distanceTo(end) / 2) / 16);
        List<Portal> nearby = getNearbyEntities(world, middle, chunkRadius, Portal.class);
        
        if (includeGlobalPortals) {
            nearby.addAll(McHelper.getGlobalPortals(world));
        }
        
        // Make a list of all portals actually intersecting with this line, and then sort them by the distance from the
        // start position. Nearest portals first.
        List<Tuple<Portal, Vector3d>> hits = new ArrayList<>();
        
        nearby.forEach(portal -> {
            if (filter == null || filter.test(portal)) {
                Vector3d intersection = portal.rayTrace(start, end);
                
                if (intersection != null) {
                    hits.add(new Tuple<>(portal, intersection));
                }
            }
        });
        
        hits.sort((pair1, pair2) -> {
            Vector3d intersection1 = pair1.getB();
            Vector3d intersection2 = pair2.getB();
            
            // Return a negative number if intersection1 is smaller (should come first)
            return (int) Math.signum(intersection1.squareDistanceTo(start) - intersection2.squareDistanceTo(
                start));
        });
        
        return hits;
    }
    
    /**
     * @see #withSwitchedContext(World, Supplier)
     */
    @OnlyIn(Dist.CLIENT)
    private static <T> T withSwitchedContextClient(ClientWorld world, Supplier<T> func) {
        boolean wasContextSwitched = BlockManipulationClient.isContextSwitched;
        Minecraft client = Minecraft.getInstance();
        ClientWorld lastWorld = client.world;
        
        try {
            BlockManipulationClient.isContextSwitched = true;
            client.world = world;
            
            return func.get();
        }
        finally {
            client.world = lastWorld;
            BlockManipulationClient.isContextSwitched = wasContextSwitched;
        }
    }
    
    /**
     * @see #withSwitchedContext(World, Supplier)
     */
    @SuppressWarnings("unused")
    private static <T> T withSwitchedContextServer(ServerWorld world, Supplier<T> func) {
        // lol
        return func.get();
    }
    
    /**
     * Execute {@code func} with the world being set to {@code world}, hopefully bypassing any issues that may be
     * related to mutating a world that is not currently set as the current world.
     * <p>
     * You may safely nest this function within other context switches. It works on both the client and the server.
     *
     * @param world The world to switch the context to. The context will be restored when {@code func} is complete.
     * @param func  The function to execute while the context is switched.
     * @param <T>   The return type of {@code func}.
     * @return Whatever {@code func} returned.
     */
    private static <T> T withSwitchedContext(World world, Supplier<T> func) {
        if (world.isRemote) {
            return withSwitchedContextClient((ClientWorld) world, func);
        }
        else {
            return withSwitchedContextServer((ServerWorld) world, func);
        }
    }
    
    /**
     * @author LoganDark
     * @see Helper#rayTrace(World, RaycastContext, boolean, List)
     */
    private static Tuple<BlockRayTraceResult, List<Portal>> rayTrace(
        World world,
        RayTraceContext context,
        boolean includeGlobalPortals,
        List<Portal> portals
    ) {
        Vector3d start = context.getStartVec();
        Vector3d end = context.getEndVec();
        
        // If we're past the max portal layer, don't let the player target behind this portal, create a missed result
        if (portals.size() > Global.maxPortalLayer) {
            Vector3d diff = end.subtract(start);
            
            return new Tuple<>(
                BlockRayTraceResult.createMiss(
                    end,
                    Direction.getFacingFromVector(diff.x, diff.y, diff.z),
                    new BlockPos(end)
                ),
                portals
            );
        }
        
        // First ray trace normally
        BlockRayTraceResult hitResult = world.rayTraceBlocks(context);
        
        List<Tuple<Portal, Vector3d>> rayTracedPortals = withSwitchedContext(
            world,
            () -> rayTracePortals(world, start, end, includeGlobalPortals, Portal::isInteractable)
        );
        
        if (rayTracedPortals.isEmpty()) {
            return new Tuple<>(hitResult, portals);
        }
        
        Tuple<Portal, Vector3d> portalHit = rayTracedPortals.get(0);
        Portal portal = portalHit.getA();
        Vector3d intersection = portalHit.getB();
        
        // If the portal is not closer, return the hit result we just got
        if (hitResult.getHitVec().squareDistanceTo(start) < intersection.squareDistanceTo(start)) {
            return new Tuple<>(hitResult, portals);
        }
        
        // If the portal is closer, recurse
        
        IERayTraceContext betterContext = (IERayTraceContext) context;
        
        betterContext
            .setStart(portal.transformPoint(intersection))
            .setEnd(portal.transformPoint(end));
        
        portals.add(portal);
        World destWorld = portal.getDestinationWorld();
        Tuple<BlockRayTraceResult, List<Portal>> recursion = withSwitchedContext(
            destWorld,
            () -> rayTrace(destWorld, context, includeGlobalPortals, portals)
        );
        
        betterContext
            .setStart(start)
            .setEnd(end);
        
        return recursion;
    }
    
    /**
     * Ray traces for blocks or whatever the {@code context} dictates.
     *
     * @param world                The world to ray trace in.
     * @param context              The ray tracing context to use. This context will be mutated as it goes but will be
     *                             returned back to normal before a result is returned to you, so you can act like it
     *                             hasn't been  mutated.
     * @param includeGlobalPortals Whether or not to include global portals in the ray trace. If this is false, then the
     *                             ray trace can pass right through them.
     * @return The BlockHitResult and the list of portals that we've passed through to get there. This list can be used
     * to transform looking directions or do whatever you want really.
     * @author LoganDark
     */
    @SuppressWarnings("WeakerAccess")
    public static Tuple<BlockRayTraceResult, List<Portal>> rayTrace(
        World world,
        RayTraceContext context,
        boolean includeGlobalPortals
    ) {
        return rayTrace(world, context, includeGlobalPortals, new ArrayList<>());
    }
    
    /**
     * @param hitResult The HitResult to check.
     * @return If the HitResult passed is either {@code null}, or of type {@link HitResult.Type#MISS}.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hitResultIsMissedOrNull(RayTraceResult hitResult) {
        return hitResult == null || hitResult.getType() == RayTraceResult.Type.MISS;
    }
    
    /**
     * @param vec  The {@link Vec3d} to get the {@link Direction} of.
     * @param axis The {@link Direction.Axis} of directions to exclude.
     * @return The {@link Direction} of the passed {@code vec}, excluding directions of axis {@code axis}.
     */
    @SuppressWarnings("WeakerAccess")
    public static Direction getFacingExcludingAxis(Vector3d vec, Direction.Axis axis) {
        return Arrays.stream(Direction.values())
            .filter(d -> d.getAxis() != axis)
            .max(Comparator.comparingDouble(
                dir -> vec.x * dir.getXOffset() + vec.y * dir.getYOffset() + vec.z * dir.getZOffset()
            ))
            .orElse(null);
    }
    
    // calculate upon first retrieval and cache it
    public static <T> Supplier<T> cached(Supplier<T> supplier) {
        return new Supplier<T>() {
            T cache = null;
            
            @Override
            public T get() {
                if (cache == null) {
                    cache = supplier.get();
                }
                Validate.notNull(cache);
                return cache;
            }
        };
    }
    
    // I cannot find existing indexOf with predicate
    public static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            T ele = list.get(i);
            if (predicate.test(ele)) {
                return i;
            }
        }
        return -1;
    }
    
    public static List<String> splitStringByLen(String str, int len) {
        List<String> result = new ArrayList<>();
        
        for (int i = 0; i * len < str.length(); i++) {
            result.add(
                str.substring(i * len, Math.min(str.length(), (i + 1) * len))
            );
        }
        
        return result;
    }
    
    // this will expand the box because the box can only be axis aligned
    public static AxisAlignedBB transformBox(
        AxisAlignedBB box, Function<Vector3d, Vector3d> function
    ) {
        List<Vector3d> result =
            Arrays.stream(eightVerticesOf(box)).map(function).collect(Collectors.toList());
        
        return new AxisAlignedBB(
            result.stream().mapToDouble(b -> b.x).min().getAsDouble(),
            result.stream().mapToDouble(b -> b.y).min().getAsDouble(),
            result.stream().mapToDouble(b -> b.z).min().getAsDouble(),
            result.stream().mapToDouble(b -> b.x).max().getAsDouble(),
            result.stream().mapToDouble(b -> b.y).max().getAsDouble(),
            result.stream().mapToDouble(b -> b.z).max().getAsDouble()
        );
    }
    
    private static double getDistanceToRange(double start, double end, double pos) {
        Validate.isTrue(end >= start);
        if (pos >= start) {
            if (pos <= end) {
                return 0;
            }
            else {
                return pos - end;
            }
        }
        else {
            return start - pos;
        }
    }
    
    public static double getDistanceToBox(AxisAlignedBB box, Vector3d point) {
        double dx = getDistanceToRange(box.minX, box.maxX, point.x);
        double dy = getDistanceToRange(box.minY, box.maxY, point.y);
        double dz = getDistanceToRange(box.minZ, box.maxZ, point.z);
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public static <T> T firstOf(List<T> list) {
        Validate.isTrue(!list.isEmpty());
        return list.get(0);
    }
    
    public static <T> T lastOf(List<T> list) {
        Validate.isTrue(!list.isEmpty());
        return list.get(list.size() - 1);
    }
    
    @Nullable
    public static <T> T combineNullable(@Nullable T a, @Nullable T b, BiFunction<T, T, T> combiner) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return combiner.apply(a, b);
    }
}
