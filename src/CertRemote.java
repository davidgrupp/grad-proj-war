import java.rmi.*;

public interface CertRemote extends Remote {
    PlayerCertificate getCert(String playerName) throws RemoteException;
}
