import java.util.*;

public class LiteBlock extends LITEchain{
	public String currhash;
	public String prevhash; 
	public String data;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); 
	public long timeStamp; 
	public int mineval;
	
	
	public String Encode() {
		String encryptOne=shaEncoder.encode(prevhash+Long.toString(timeStamp)+Integer.toString(mineval)+data);
		String key = "133457799BBCDFF1";
		String s0 = encryptOne.substring(0,16);
		String s1 = encryptOne.substring(16,32);
		String s2 = encryptOne.substring(32,48);
		String s3 = encryptOne.substring(48,64);

		String s00 = dse.encrypt(s0,key);
		String s11 = dse.encrypt(s1,key);
		String s22 = dse.encrypt(s2,key);
		String s33 = dse.encrypt(s3,key);
		String res = s00.concat(s11).concat(s22).concat(s33);
		return res;
	}
	
	public void Mine(int level)
	{
		char[] zeroes=new char[level];
		int var=level;
		data = shaEncoder.getMerkleRoot(transactions);
		
		while(--var>=0)
			zeroes[var]='0';
		
		String req= new String(zeroes);
		
		for(;!req.equals(currhash.substring(0,level));)
		{
			mineval++;
			currhash=Encode();
		}
		System.out.println("Block Mined--"+currhash);
	}
	
	public LiteBlock(String prevhash)
	{
		this.prevhash=prevhash;
		this.timeStamp=new Date().getTime();
		this.currhash=Encode();
	}

	public boolean addTransaction(Transaction transaction) {
		if(transaction == null) return false;		
		if((prevhash != "0")) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction);

		for(int i=0;i<5;i++)
		{
			if(Wallets.get(userName.get(i)).publicKey==transaction.sender)
			{
				senderList.add(userName.get(i));
			}

			if(Wallets.get(userName.get(i)).publicKey==transaction.reciepient)
			{
				reciepientList.add(userName.get(i));
			}
		}

		amountList.add(transaction.value);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	
}
