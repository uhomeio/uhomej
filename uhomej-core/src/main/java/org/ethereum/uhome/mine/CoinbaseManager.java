package org.ethereum.uhome.mine;


import org.ethereum.core.Blockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CoinbaseManager implements CoinbaseIfc {

    private static final Logger logger = LoggerFactory.getLogger("uhome");
    @Autowired
    private Blockchain blockchain;

    public static long authorizedBlockPeriod = 5000;

    public static double hashRatePercent = 0.05;

    public static long maxMinedBlockNumber = Math.round(authorizedBlockPeriod * hashRatePercent);

    private Map<Long, byte[]> blockChainCoinbase = new ConcurrentHashMap<>();

    private Map<byte[], List<Consensus>> coinbaseStateMap = new ConcurrentHashMap<>();

    private Map<Long, List<byte[]>> authorizedCoinbaseAtBlockChain = new ConcurrentHashMap<>();

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

        assignBlockLegalCoinbase(coinbase, startBlockNumber);
    }

    @Override
    public boolean isLegalCoinbase(byte[] coinbase, long blockNumber) {

        if (coinbaseStateMap.containsKey(coinbase)) {
            List<Consensus> consensuses = coinbaseStateMap.get(coinbase);
            for (Consensus consensus : consensuses) {
                if (blockNumber >= consensus.getStartBlockNumber() || blockNumber <= consensus.getEndBlockNumber()) {
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

        if (authorizedCoinbaseAtBlockChain.get(blockNumber).contains(coinbase)) {

            if (isLegalCoinbase(coinbase, blockNumber)) {

//        master block has changed, roll back the record
                byte[] oldCoinbase = blockChainCoinbase.get(blockNumber);
                if (null != oldCoinbase && !oldCoinbase.equals(coinbase)) {

//            roll back the old recording
                    List<Consensus> consensuses = coinbaseStateMap.get(oldCoinbase);
                    for (Consensus consensus : consensuses) {
                        if (blockNumber >= consensus.getStartBlockNumber() && blockNumber <= consensus.getEndBlockNumber()) {
                            if (consensus.getMinedBlocks() > 0) {
                                consensus.setMinedBlocks(consensus.getMinedBlocks() - 1);
                                coinbaseStateMap.put(oldCoinbase, consensuses);
                                break;
                            }
                        }
                    }
                }

//        add the mined block record
                List<Consensus> consensusList = coinbaseStateMap.get(coinbase);
                for (Consensus consensus : consensusList) {
                    if (blockNumber >= consensus.getStartBlockNumber() && blockNumber <= consensus.getEndBlockNumber()) {
                        if (consensus.getMinedBlocks() < consensus.getLimitBlocks()) {
                            consensus.setMinedBlocks(consensus.getMinedBlocks() + 1);
                            coinbaseStateMap.put(coinbase, consensusList);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<byte[]> getAllowableCoinbase(long blockNumber) {

//        the mined block authorized coinbase
        if (blockNumber <= blockchain.getBestBlock().getNumber()) {
            return authorizedCoinbaseAtBlockChain.get(blockNumber);
        }

//        the future block authorized coinbase
        List<byte[]> coinbaseList = new ArrayList<>();
        for (Map.Entry<byte[], List<Consensus>> entry : coinbaseStateMap.entrySet()) {
            List<Consensus> consensusList = entry.getValue();
            for (Consensus consensus : consensusList) {
                if (blockNumber >= consensus.getStartBlockNumber() && blockNumber <= consensus.getEndBlockNumber()) {
                    if (consensus.getMinedBlocks() < consensus.getLimitBlocks()) {
                        coinbaseList.add(entry.getKey());
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
            if (nextBlockNumber >= consensus.getStartBlockNumber() && nextBlockNumber <= consensus.getEndBlockNumber()) {
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
        consensus.setEndBlockNumber(startBlockNumber + authorizedBlockPeriod - 1);
        consensus.setLimitBlocks(maxMinedBlockNumber);
        consensus.setMinedBlocks(0);

        return consensus;
    }


    private void assignBlockLegalCoinbase(byte[] coinbase, long startBlockNumber) {

        long endBlockNumber = startBlockNumber + authorizedBlockPeriod - 1;
        for (; startBlockNumber <= endBlockNumber; startBlockNumber++) {

            List<byte[]> coinbaseList = new ArrayList<>();

            if (authorizedCoinbaseAtBlockChain.containsKey(startBlockNumber)) {
                coinbaseList = authorizedCoinbaseAtBlockChain.get(startBlockNumber);
            }
            coinbaseList.add(coinbase);

            authorizedCoinbaseAtBlockChain.put(startBlockNumber, coinbaseList);
        }

    }

}
