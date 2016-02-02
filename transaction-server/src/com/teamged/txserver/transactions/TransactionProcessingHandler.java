package com.teamged.txserver.transactions;

import com.teamged.txserver.TransactionMonitor;
import com.teamged.txserver.database.DataProxy;

/**
 * Created by danie on 2016-02-01.
 */
public class TransactionProcessingHandler implements Runnable {
    private final String userName;

    public TransactionProcessingHandler(String user) {
        userName = user;
    }

    @Override
    public void run() {
        // Note that the UserQueueObject is a very unsafe class - proper locking and thread safety is the
        // responsibility of this handler
        UserQueueObject userObj = TransactionMonitor.GetUserObject(userName);
        if (userObj != null) {
            if (!userObj.tryClaimProcessingFlag()) {
                // Put the name back in the queue to handle later when there's no conflict
                TransactionMonitor.PutRequestQueue(userName);
            } else {
                try {
                    // Process the transaction
                    int count = userObj.getNextProcessCount();
                    System.out.println("TxProcH: Count is - " + count);

                    while (count > 0) {
                        TransactionObject txObject = userObj.getNextTransactionObject();
                        if (txObject == null) {
                            System.out.println("TxProcH: txObject was null");
                            break;
                        } else {
                            String resp = DataProxy.dbOperation(txObject);
                            System.out.println(resp);   // TODO: Handle the response.
                        }

                        count--;
                    }
                } finally {
                    // This must run to prevent locking out a user on a thread failure
                    userObj.releaseProcessingFlag();
                }
            }
        } else {
            System.out.println("Couldn't find user information in TransactionMonitor for user " + userName);
        }
    }
}
