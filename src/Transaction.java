import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId; 
	public PublicKey sender; 
	public PublicKey reciepient; 
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; 
	
	
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	private String calulateHash() {
		sequence++; 
		return shaEncoder.encode(
				shaEncoder.getStringFromKey(sender) +
				shaEncoder.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}

    public void generateSignature(PrivateKey privateKey) {
        String data = shaEncoder.getStringFromKey(sender) + shaEncoder.getStringFromKey(reciepient) + Float.toString(value)	;
        signature = shaEncoder.applyECDSASig(privateKey,data);		
    }
    public boolean verifiySignature() {
        String data = shaEncoder.getStringFromKey(sender) + shaEncoder.getStringFromKey(reciepient) + Float.toString(value)	;
        return shaEncoder.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
            
        if(verifiySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
                
        for(TransactionInput i : inputs) {
            i.UTXO = LITEchain.UTXOs.get(i.transactionOutputId);
        }
        
        float leftOver = getInputsValue() - value;
        transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.reciepient, value,transactionId));
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));		
                
        for(TransactionOutput o : outputs) {
            LITEchain.UTXOs.put(o.id , o);
        }
        
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; 
            LITEchain.UTXOs.remove(i.UTXO.id);
        }
        
        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; 
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}
