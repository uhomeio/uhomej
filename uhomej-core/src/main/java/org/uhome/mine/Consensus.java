package org.uhome.mine;

public class Consensus {

    private long startBlockNumber;

    private long endBlockNumber;

    private long limitBlocks;

    private int minedBlocks;

    public long getStartBlockNumber() {
        return startBlockNumber;
    }

    public void setStartBlockNumber(long startBlockNumber) {
        this.startBlockNumber = startBlockNumber;
    }

    public long getEndBlockNumber() {
        return endBlockNumber;
    }

    public void setEndBlockNumber(long endBlockNumber) {
        this.endBlockNumber = endBlockNumber;
    }

    public long getLimitBlocks() {
        return limitBlocks;
    }

    public void setLimitBlocks(long limitBlocks) {
        this.limitBlocks = limitBlocks;
    }

    public int getMinedBlocks() {
        return minedBlocks;
    }

    public void setMinedBlocks(int minedBlocks) {
        this.minedBlocks = minedBlocks;
    }
}
