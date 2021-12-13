package sugimomoto;

import java.net.InetAddress;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;

/*
https://square.github.io/okhttp/events/
*/
public class PrintingEventListner extends EventListener {
    
    private long callStartNanos;

    private void printEvent(String name){
        long nowNanos = System.nanoTime();
        if(name.equals("callStart")){
            callStartNanos = nowNanos;
        }

        long elapsedNanos = nowNanos - callStartNanos;
        System.out.printf("%.3f %s%n", elapsedNanos / 1000000000d, name);
    }

    @Override
    public void callEnd(Call call) {
        printEvent("calldEnd");
    }

    @Override
    public void callStart(Call call) {
        printEvent("calldStart");
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        printEvent("dnsEnd");
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        printEvent("dnsStart");
    }
}
