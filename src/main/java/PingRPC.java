import org.apache.hadoop.ipc.ProtocolInfo;

/**
 * Created by prasanthj on 2020-06-19.
 */
// https://cwiki.apache.org/confluence/display/HADOOP2/HadoopRpc
@ProtocolInfo(protocolName = "ping", protocolVersion = 1)
interface PingRPC {
  String ping();
}