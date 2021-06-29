import java.security.PublicKey;

public class Sent {
    public String id;
	public PublicKey reciepient;
	public float value;
	public String parentTransactionId;
	
	public Sent(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = shaEncoder.encode(shaEncoder.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
}