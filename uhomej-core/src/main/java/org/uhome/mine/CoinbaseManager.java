package org.uhome.mine;


import org.apache.commons.collections4.map.HashedMap;
import org.ethereum.core.Blockchain;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinbaseManager implements CoinbaseIfc {

    @Autowired
    private Blockchain blockchain;

    public static long authorizedBlockPeriod = 5000;

    public static double hashRatePercent = 0.05;

    public static long maxMinedBlockNumber = Math.round(authorizedBlockPeriod * hashRatePercent);

    private Map<Long, byte[]> blockChainCoinbase = new HashMap<>();

    private Map<byte[], List<Consensus>> coinbaseStateMap = new HashedMap<>();


    @Override
    public void addNewCoinbase(byte[] coinbase, long startBlockNumber) {

        Consensus consensus = createConsensus(coinbase, startBlockNumber);
        List<Consensus> consensuses;
        if (coinbaseStateMap.containsKey(coinbase)) {
            consensuses = coinbaseStateMap.get(coinbase);
        } else {
            consensuses = new ArrayList<>();
        }

        consensuses.add(consensus);
        coinbaseStateMap.put(coinbase, consensuses);
    }

    @Override
    public boolean isLegalCoinbase(byte[] coinbase, long blockNumber) {

        if (coinbaseStateMap.containsKey(coinbase)) {
            List<Consensus> consensuses = coinbaseStateMap.get(coinbase);
            for (Consensus consensus : consensuses) {
                if (blockNumber < consensus.getStartBlockNumber() || blockNumber > consensus.getEndBlockNumber()) {
                    if (consensus.getMinedBlocks() < consensus.getLimitBlocks()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void updateBlockRecord(long blockNumber, byte[] coinbase) {

//        master block has changed
        byte[] oldCoinbase = blockChainCoinbase.get(blockNumber);
        if (null != oldCoinbase) {

//            is the same block information
            if (oldCoinbase.equals(coinbase)) return;
//           is not valid coinbase
            if (!isLegalCoinbase(coinbase, blockNumber)) return;

//            roll back the old recording
            List<Consensus> consensuses = coinbaseStateMap.get(oldCoinbase);
            for (Consensus consensus : consensuses) {
                if (blockNumber < consensus.getStartBlockNumber() || blockNumber > consensus.getEndBlockNumber()) {
                    if (consensus.getMinedBlocks() > 1) {
                        consensus.setMinedBlocks(consensus.getMinedBlocks() - 1);
                        coinbaseStateMap.put(oldCoinbase, consensuses);
                        break;
                    }
                }
            }
        } else {
            if (!isLegalCoinbase(coinbase, blockNumber)) return;
        }

//        add the mined block record
        List<Consensus> consensusList = coinbaseStateMap.get(coinbase);
        for (Consensus consensus : consensusList) {
            if (blockNumber < consensus.getStartBlockNumber() || blockNumber > consensus.getEndBlockNumber()) {
                if (consensus.getMinedBlocks() <= consensus.getLimitBlocks()) {
                    consensus.setMinedBlocks(consensus.getMinedBlocks() + 1);
                    coinbaseStateMap.put(coinbase, consensusList);
                    break;
                }
            }
        }
    }

    @Override
    public List<byte[]> getAllowableCoinbase(long blockNumber) {

        List<byte[]> coinbaseList = new ArrayList<>();

        boolean future = true;
        if (blockchain.getBestBlock().getNumber() < blockNumber) future = false;

//         get future allowed miner or get authorised miner in the past
        for (Map.Entry<byte[], List<Consensus>> entry : coinbaseStateMap.entrySet()) {
            List<Consensus> consensusList = entry.getValue();
            for (Consensus consensus : consensusList) {
                if (blockNumber < consensus.getStartBlockNumber() || blockNumber > consensus.getEndBlockNumber()) {
                    if (!future || (future && consensus.getMinedBlocks() < consensus.getLimitBlocks())) {
                        coinbaseList.add(entry.getKey());
                        break;
                    }
                }
            }
        }

        return coinbaseList;
    }

    @Override
    public Map<byte[], Integer> getCoinbasePerformance(long startBlockNumber, long endBlockNumber) {

        Map<byte[], Integer> map = new HashMap<>();
        for (; startBlockNumber <= endBlockNumber; startBlockNumber++) {
            byte[] coinbase = blockChainCoinbase.get(startBlockNumber);
            if (map.containsKey(coinbase)) {
                map.put(coinbase, map.get(coinbase) + 1);
            } else {
                map.put(coinbase, 1);
            }
        }

        return map;
    }

    @Override
    public long residualPOW(byte[] coinbase) {

        long nextBlockNumber = blockchain.getBestBlock().getNumber() + 1;
        List<Consensus> consensusList = coinbaseStateMap.get(coinbase);
        for (Consensus consensus : consensusList) {
            if (nextBlockNumber < consensus.getStartBlockNumber() && nextBlockNumber > consensus.getEndBlockNumber()) {
                if (consensus.getMinedBlocks() < consensus.getLimitBlocks()) {
                    return consensus.getLimitBlocks() - consensus.getMinedBlocks();
                }
            }
        }

        return 0;
    }

    private Consensus createConsensus(byte[] coinbase, long startBlockNumber) {

        Consensus consensus = new Consensus();
        consensus.setStartBlockNumber(startBlockNumber);
        consensus.setEndBlockNumber(startBlockNumber + authorizedBlockPeriod);
        consensus.setLimitBlocks(maxMinedBlockNumber);
        consensus.setMinedBlocks(0);

        return consensus;
    }

}
