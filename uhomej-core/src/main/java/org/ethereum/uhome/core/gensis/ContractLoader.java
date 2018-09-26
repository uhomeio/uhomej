package org.ethereum.uhome.core.gensis;

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.core.genesis.GenesisJson;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.GasPriceTracker;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.ethereum.solidity.compiler.SolidityCompiler.Options.*;
import static org.ethereum.util.ByteUtil.hexStringToBytes;

public class ContractLoader {

    public static List<Genesis.PreTransaction> generatePreTransaction(BlockchainNetConfig blockchainNetConfig, GenesisJson genesisJson) {

        List<Genesis.PreTransaction> preTransactions = new ArrayList<>();

        if (null != genesisJson.getContact()) {
            for (Map.Entry<String, List<GenesisJson.PreDeployedContact>> entry : genesisJson.getContact().entrySet()) {

                final byte[] privateKey = hexStringToBytes(entry.getKey());
                ECKey ecKey = ECKey.fromPrivate(privateKey);

                List<Transaction> txs = new ArrayList<Transaction>();
                BigInteger nonce = blockchainNetConfig.getCommonConstants().getInitialNonce();

                for (GenesisJson.PreDeployedContact contact : entry.getValue()) {

                    String path = new Object() {
                        public String getPath() {
                            return this.getClass().getClassLoader().getResource(contact.path).getPath();
                        }
                    }.getPath().substring(1);

                    File file = new File(path);

                    if (!file.exists() || file.isDirectory()) {
                        throw new RuntimeException("Source Contract file is not exist :\n");
                    }

                    try {
//                        compatible if this contact import parent folder file
                        SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(file.getParentFile().getParentFile()));

                        SolidityCompiler.Result result = SolidityCompiler.compile(file, true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
                        if (result.isFailed()) {
                            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
                        }

                        CompilationResult res = CompilationResult.parse(result.output);
                        if (res.getContracts().isEmpty()) {
                            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
                        }

                        CompilationResult.ContractMetadata metadata = res.getContracts().iterator().next();
                        if (metadata.bin == null || metadata.bin.isEmpty()) {
                            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
                        }

                        Transaction tx = new Transaction(
                                ByteUtil.bigIntegerToBytes(nonce),
                                ByteUtil.longToBytesNoLeadZeroes((new GasPriceTracker()).getGasPrice()),
                                ByteUtil.longToBytesNoLeadZeroes(50_000_000L),
                                new byte[0],
                                ByteUtil.longToBytesNoLeadZeroes(0),
                                Hex.decode(metadata.bin),
                                null);

                        tx.sign(ecKey);
                        txs.add(tx);

                        nonce = nonce.add(BigInteger.ONE);

                        System.out.println("genesis transaction: " + tx.toString());

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("IOException: Failed to access contact file" + file.getAbsolutePath() + " \n");
                    }
                }

                preTransactions.add(new Genesis.PreTransaction(ecKey.getAddress(), txs));
            }
        }
        return preTransactions;
    }

}
