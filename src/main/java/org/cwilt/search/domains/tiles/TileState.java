package org.cwilt.search.domains.tiles;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.cwilt.search.algs.basic.bestfirst.WAStar;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.utils.TemporaryLoadAndWritePath;
import org.cwilt.search.utils.basic.Permutation;
public class TileState extends org.cwilt.search.search.SearchState implements Serializable {
    public double distTo(SearchState other) {
        TileState o = (TileState) other;
        return prob.mdFromScratch(this, o);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -339674260116451989L;

    protected static class TileBoard implements Serializable {
        /**
		 * 
		 */
        private static final long serialVersionUID = -3056582577711905076L;
        public final char[] c;

        public TileBoard(char[] c) {
            this.c = c;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();

            sb.append("[");

            for (int i = 0; i < c.length; i++) {
                int val = c[i];
                sb.append(val);

                if (i != c.length - 1) {
                    sb.append(", ");
                }
            }

            sb.append("]");

            return sb.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(c);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TileBoard other = (TileBoard) obj;
            if (!Arrays.equals(c, other.c))
                return false;
            return true;
        }
    }

    private final DIRECTION lastMove;
    protected final TileProblem prob;
    protected final TileBoard b;
    protected double manhattanDistance;
    protected int manhattanDistanceD;
    protected int blankPosition;

    TileState(TileProblem p, TileBoard b) {
        this.prob = p;
        this.b = b;
        this.lastMove = DIRECTION.NONE;
    }

    public TileState(TileProblem p, boolean goal) {
        this.lastMove = DIRECTION.NONE;
        this.prob = p;

        if (!prob.calcMD()) {
            b = new TileBoard(prob.copyArray());
            this.manhattanDistance = prob.getH(this);
            this.manhattanDistanceD = prob.getD(this);
            for (int i = 0; i < prob.getAcross() * prob.getDown(); i++) {
                if (b.c[i] == 0)
                    this.blankPosition = i;
            }
        } else if (!goal) {
            b = new TileBoard(prob.copyArray());
            this.manhattanDistance = prob.mdFromScratch(this);
            this.manhattanDistanceD = prob.mddFromScratch(this);
            for (int i = 0; i < prob.getAcross() * prob.getDown(); i++) {
                if (b.c[i] == 0)
                    this.blankPosition = i;
            }
        } else {
            char[] c = new char[p.getDown() * p.getAcross()];
            b = new TileBoard(c);
            for (char i = 0; i < c.length; i++) {
                b.c[i] = i;
            }
            this.manhattanDistance = 0;
            this.manhattanDistanceD = 0;
        }
    }

    public void recalculateMD() {
        this.manhattanDistance = calculateMD(this, 0);
    }

    protected double calculateMD(TileState original, int movedTile) {
        double delta = prob.mdIncrement(movedTile, original.blankPosition,
                blankPosition);
        double toReturn = -delta + original.manhattanDistance;
        if (toReturn < -.0000000001) {
            System.err.println(toReturn);
            System.err.printf("mdIncr = %f original = %f\n", prob.mdIncrement(
                    movedTile, original.blankPosition, blankPosition),
                    original.manhattanDistance);
            assert (false);
        }
        return toReturn;
    }

    protected int calculateMDD(TileState original, int movedTile) {
        if (prob.calculateD()) {
            int delta = prob.mddIncrement(movedTile, original.blankPosition,
                    blankPosition);
            int toReturn = -delta + original.manhattanDistanceD;
            return toReturn;
        } else
            return 0;
    }

    public void recalculateMDD() {
        this.manhattanDistanceD = calculateMDD(this, 0);
    }

    protected TileState move(DIRECTION d) {
        return new TileState(this, d);
    }

    protected TileState(TileState original, DIRECTION d) {
        assert (d != DIRECTION.NONE);
        this.lastMove = d;
        prob = original.prob;
        b = new TileBoard(Arrays.copyOf(original.b.c, original.b.c.length));
        switch (d) {
        case UP:
            blankPosition = original.blankPosition - prob.getAcross();
            break;
        case DOWN:
            blankPosition = original.blankPosition + prob.getAcross();
            break;
        case LEFT:
            blankPosition = original.blankPosition - 1;
            break;
        case RIGHT:
            blankPosition = original.blankPosition + 1;
            break;
        case NONE:
            assert (false);
            break;
        }
        int movedTile = b.c[blankPosition];

        if (!prob.calcMD()) {
            b.c[blankPosition] = 0;
            b.c[original.blankPosition] = (char) movedTile;
            this.manhattanDistance = prob.getH(this);
            this.manhattanDistanceD = prob.getD(this);
        }

        else if (!(this instanceof AbstractedTileState)) {
            manhattanDistance = calculateMD(original, movedTile);
            manhattanDistanceD = calculateMDD(original, movedTile);
            b.c[blankPosition] = 0;
            b.c[original.blankPosition] = (char) movedTile;
            assert (calculateMD(this, movedTile) == manhattanDistance);
        } else {
            b.c[blankPosition] = 0;
            b.c[original.blankPosition] = (char) movedTile;
            manhattanDistance = calculateMD(this, movedTile);
            manhattanDistanceD = calculateMDD(original, movedTile);
        }

        // double freshMD = prob.mdFromScratch(this);
        // if(Math.abs(freshMD - manhattanDistance) > .000001){
        // System.err.printf("this.md = %f freshMD = %f\n",
        // this.manhattanDistance, freshMD);
        // assert(false);
        // }
    }

    protected enum DIRECTION {
        UP, LEFT, RIGHT, DOWN, NONE
    }

    private boolean canLeft(boolean pruneParent) {
        boolean back = lastMove != DIRECTION.RIGHT || !pruneParent;
        boolean positional = blankPosition % prob.getAcross() != 0 && back;
        if (!positional)
            return false;
        int movingTile = b.c[blankPosition - 1];
        return prob.canMove(movingTile);
    }

    private boolean canRight(boolean pruneParent) {
        boolean back = lastMove != DIRECTION.LEFT || !pruneParent;
        boolean positional = blankPosition % prob.getAcross() != prob
                .getAcross() - 1 && back;
        if (!positional)
            return false;
        int movingTile = b.c[blankPosition + 1];
        return prob.canMove(movingTile);
    }

    private boolean canDown(boolean pruneParent) {
        boolean back = lastMove != DIRECTION.UP || !pruneParent;
        boolean positional = blankPosition / prob.getAcross() != prob.getDown() - 1
                && back;
        if (!positional)
            return false;
        int movingTile = b.c[blankPosition + prob.getAcross()];
        return prob.canMove(movingTile);
    }

    private boolean canUp(boolean pruneParent) {
        boolean back = lastMove != DIRECTION.DOWN || !pruneParent;
        boolean positional = blankPosition / prob.getAcross() != 0 && back;
        if (!positional)
            return false;
        int movingTile = b.c[blankPosition - prob.getAcross()];
        return prob.canMove(movingTile);
    }
    
    @Override
    public ArrayList<Child> expand() {
        ArrayList<Child> children = new ArrayList<Child>(3);
        boolean left = canLeft(true);
        boolean right = canRight(true);
        boolean up = canUp(true);
        boolean down = canDown(true);
        if (left) {
            int tileID = b.c[blankPosition - 1];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.LEFT), prob.getCost(tileID)));
        }
        if (right) {
            int tileID = b.c[blankPosition + 1];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.RIGHT), prob.getCost(tileID)));
        }
        if (up) {
            int tileID = b.c[blankPosition - prob.getAcross()];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.UP), prob.getCost(tileID)));
        }
        if (down) {
            int tileID = b.c[blankPosition + prob.getAcross()];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.DOWN), prob.getCost(tileID)));
        }
        return children;
    }

    @Override
    public boolean isGoal() {
        return prob.isGoal(b.c);
    }

    @Override
    public Object getKey() {
        return b;
    }

    @Override
    public int hashCode() {
        return b.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        TileState other = (TileState) obj;
        if (this.blankPosition != other.blankPosition)
            return false;
        if (!Arrays.equals(b.c, other.b.c))
            return false;
        return true;
    }

    @Override
    public int lexOrder(SearchState s) {
        TileState ts = (TileState) s;
        for (int i = 0; i < b.c.length; i++) {
            if (this.b.c[i] > ts.b.c[i])
                return 1;
            if (this.b.c[i] < ts.b.c[i])
                return -1;
        }
        assert (this.equals(s));
        return 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(TileProblem.printTileArray(b.c, prob.getAcross(),
                prob.getDown()));
        buf.append("heuristic: ");
        buf.append(manhattanDistance);
        buf.append(" d: ");
        buf.append(manhattanDistanceD);
        return buf.toString();
    }

    @Override
    public double h() {
        return manhattanDistance;
    }

    @Override
    public int d() {
        return (int) manhattanDistanceD;
    }

    public ArrayList<Child> reverseExpand() {
        ArrayList<Child> children = new ArrayList<Child>();
        boolean left = canLeft(true);
        boolean right = canRight(true);
        boolean up = canUp(true);
        boolean down = canDown(true);
        if (left) {
            int tileID = b.c[blankPosition - 1];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.LEFT), prob.getCost(tileID)));
        }
        if (right) {
            int tileID = b.c[blankPosition + 1];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.RIGHT), prob.getCost(tileID)));
        }
        if (up) {
            int tileID = b.c[blankPosition - prob.getAcross()];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.UP), prob.getCost(tileID)));
        }
        if (down) {
            int tileID = b.c[blankPosition + prob.getAcross()];
//            double cost = prob.getCost(tileID);
//            assert (cost > 0);
            children.add(new Child(move(DIRECTION.DOWN), prob.getCost(tileID)));
        }
        return children;
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        TileProblem p = new TileProblem(TemporaryLoadAndWritePath.getTempPath()
                + "/tiledata/1", "unit", null);
        WAStar wa = new WAStar(p, new org.cwilt.search.search.Limit(), 10);
        wa.solve();
        wa.printSearchData(System.err);
    }

    @Override
    public long perfectHash() {
        assert (prob.getAcross() * prob.getDown() < 20);
        return Permutation.rank(b.c);
    }

    @Override
    public double convertToChild(int childID, int parentID) {
        TileState original = this;
        DIRECTION d;
        switch (childID) {
        case 2:
            d = DIRECTION.LEFT;
            if (!this.canLeft(false))
                return -1;
            break;
        case 1:
            d = DIRECTION.RIGHT;
            if (!this.canRight(false))
                return -1;
            break;
        case 0:
            d = DIRECTION.UP;
            if (!this.canUp(false))
                return -1;
            break;
        case 3:
            d = DIRECTION.DOWN;
            if (!this.canDown(false))
                return -1;
            break;
        default:
            d = DIRECTION.NONE;
        }

        int oldBlank = blankPosition;

        switch (d) {
        case UP:
            blankPosition = original.blankPosition - prob.getAcross();
            break;
        case DOWN:
            blankPosition = original.blankPosition + prob.getAcross();
            break;
        case LEFT:
            blankPosition = original.blankPosition - 1;
            break;
        case RIGHT:
            blankPosition = original.blankPosition + 1;
            break;
        case NONE:
            assert (false);
            throw new RuntimeException("invalid childID");
        }
        int movedTile = b.c[blankPosition];

        manhattanDistance -= prob.mdIncrement(movedTile, oldBlank,
                blankPosition);
        if (prob.calculateD())
            manhattanDistanceD -= prob.mddIncrement(movedTile, oldBlank,
                    blankPosition);
        b.c[blankPosition] = 0;
        b.c[oldBlank] = (char) movedTile;
        assert (calculateMD(this, movedTile) == manhattanDistance);
        return prob.getCost(movedTile);
    }

    public int inverseChild(int childID) {
        if (childID == 0)
            return 3;
        if (childID == 1)
            return 2;
        if (childID == 2)
            return 1;
        if (childID == 3)
            return 0;
        throw new RuntimeException("Child ID");
    }

    @Override
    public int nChildren() {
        return 4;
    }
}