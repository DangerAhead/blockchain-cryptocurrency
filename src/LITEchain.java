import java.io.Console;
import java.security.Security;
import java.util.*;
import com.mongodb.client.MongoCursor;


import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

public class LITEchain {
	
	public static ArrayList<LiteBlock> blockchain = new ArrayList<LiteBlock>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	public static HashMap<String,String> Users = new HashMap<String,String>();
	public static HashMap<String,Wallet> Wallets = new HashMap<String,Wallet>();
	public static ArrayList<String> userName = new ArrayList<String>();
	public static ArrayList<String> senderList = new ArrayList<String>();
	public static ArrayList<String> reciepientList = new ArrayList<String>();
	public static ArrayList<Float> amountList = new ArrayList<Float>();
	
	
	
	public static Boolean chainCheck() {
		LiteBlock currentBlock; 
		LiteBlock previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); 
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			if(!currentBlock.currhash.equals(currentBlock.Encode()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			if(!previousBlock.currhash.equals(currentBlock.prevhash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			if(!currentBlock.currhash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}

	
	public static void main(String[] args) throws InterruptedException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 

		

		userName.add("bank");
		userName.add("satvik");
		userName.add("akshit");
		userName.add("vatsu");
		userName.add("aishvarya");
		

		Wallet coinbase = new Wallet();

		for(int i=0;i<userName.size();i++)
		{
			Users.put(userName.get(i),userName.get(i));
			Wallets.put(userName.get(i),new Wallet());
		}

		genesisTransaction = new Transaction(coinbase.publicKey, Wallets.get(userName.get(0)).publicKey, 10000000f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);		
		genesisTransaction.transactionId = "0"; 
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); 
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); 
		LiteBlock genesis = new LiteBlock("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		

		String prevHash = genesis.currhash;

		Console cnsl = System.console();

		try (var mongoClient = MongoClients.create("mongodb://localhost:27017")){

		var database = mongoClient.getDatabase("mongojava1");

		MongoCollection<Document> collection = database.getCollection("users1");

		try (MongoCursor<Document> cur = collection.find().iterator()) {

			while (cur.hasNext()) {

				var doc = cur.next();
				var users = new ArrayList <> (doc.values());

				//System.out.printf("%s: %s%n", users.get(1), users.get(2));

				LiteBlock block = new LiteBlock(prevHash);
				System.out.println(users.get(1)+" "+users.get(2)+" "+users.get(3));
				String temp = users.get(3).toString();
				float amount = Float.parseFloat(temp);
				block.addTransaction(Wallets.get((String)users.get(1)).sendFunds(Wallets.get((String)users.get(2)).publicKey, amount));
				addBlock(block);
				//System.out.println(name+" current balance is: " + Wallets.get(name).getBalance());
				prevHash=block.currhash;

			}
		}
		
		while(true)
		{
			System.out.println("Enter 1 to login as admin and 2 to enter as normal user: ");
			int num=Integer.parseInt(cnsl.readLine());
			if(num==1)
			{
				
				String name="bank";
				
				String pass = String.valueOf(cnsl.readPassword("Enter password: "));
				if(Users.containsKey(name) && pass.equals("bank"))
				{
					System.out.println("Logged In");
				}

				else
				{
					System.out.println("Wrong credentials");
					continue;
				}

				String name2 = cnsl.readLine("Enter recipient name: ");
				float amount = Float.parseFloat(cnsl.readLine("Enter amount to transfer: "));

				var d1 = new Document();
				d1.append("name",name);
				d1.append("name2",name2);
				d1.append("amount",amount);

				collection.insertOne(d1);

				LiteBlock block = new LiteBlock(prevHash);
				block.addTransaction(Wallets.get(name).sendFunds(Wallets.get(name2).publicKey, amount));
				addBlock(block);
				System.out.println(name+" current balance is: " + Wallets.get(name).getBalance());
				prevHash=block.currhash;
			}

			else
			{
				String name;
				name = cnsl.readLine("Enter username: ");
				String pass = String.valueOf(cnsl.readPassword("Enter password: "));
				if(Users.containsKey(name) && pass.equals(Users.get(name)))
				{
					System.out.println("Logged In");
				}

				else
				{
					System.out.println("Wrong credentials");
					continue;
				}

				System.out.println("Enter 1 to view transaction history and 2 to do a transaction: ");
				num=Integer.parseInt(cnsl.readLine());

				if(num==2)
				{
					String name2 = cnsl.readLine("Enter recipient name: ");
					float amount = Float.parseFloat(cnsl.readLine("Enter amount to transfer: "));

					var d1 = new Document();
					d1.append("name",name);
					d1.append("name2",name2);
					d1.append("amount",amount);

					collection.insertOne(d1);

					LiteBlock block = new LiteBlock(prevHash);
					block.addTransaction(Wallets.get(name).sendFunds(Wallets.get(name2).publicKey, amount));
					addBlock(block);
					System.out.println(name+" current balance is: " + Wallets.get(name).getBalance());
					prevHash=block.currhash;

				}

				else
				{
					try (MongoCursor<Document> cur = collection.find().iterator()) {

						while (cur.hasNext()) {
			
							var doc = cur.next();
							var users = new ArrayList <> (doc.values());
							
							String sender = users.get(1).toString();
							String reciever = users.get(2).toString();
							String amount = users.get(3).toString();
							
							if(sender.equals(name))
							{
								System.out.println(sender+" sends "+amount+" to "+reciever);
							}
							
							if(reciever.equals(name))
							{
								System.out.println(reciever+" recieves "+amount+" from "+sender);
							}
						}
					}
				}
			}
		}
	}
		
	}

	public static void addBlock(LiteBlock newBlock) {
		newBlock.Mine(difficulty);
		blockchain.add(newBlock);
	}


}
