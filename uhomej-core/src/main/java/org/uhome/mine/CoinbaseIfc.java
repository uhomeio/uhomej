package org.uhome.mine;

import java.util.List;
import java.util.Map;

public interface CoinbaseIfc {

    public void addNewCoinbase(byte[] coinbase, long startBlock);

    public boolean isLegalCoinbase(byte[] coinbase, long blockNumber);

    public void updateBlockRecord(long blockNumber, byte[] coinbase);

    public List<byte[]> getAllowableCoinbase(long blockNumber);

    public Map<byte[],Integer> getCoinbasePerformance(long startBlockNumber, long endBlockNumber);

    public long residualPOW(byte[] coinbase);

}
