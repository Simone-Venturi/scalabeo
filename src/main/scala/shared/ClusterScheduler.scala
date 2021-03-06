package shared

import java.util.concurrent.TimeUnit
import akka.actor.Cancellable
import akka.cluster.Cluster
import scala.concurrent.duration.Duration

/** Scheduler che esegue task ripetutamente;
 *  mette a disposizione metodi per:
 *    - avviare lo scheduling del task corrente
 *    - stoppare lo scheduling del task corrente
 *    - sostituire lo scheduling corrente
 */
sealed trait CustomScheduler{
  /** avvia l'esecuzione del task preimpostato */
  def startTask():Unit
  /** stoppa l'esecuzione del task corrente */
  def stopTask(): Unit
  /** stoppa il task schedulato,se in esecuzione, lo sostituisce e avvia il nuovo task
   * @param newTask = un nuovo task da eseguire con lo scheduler
   */
  def replaceBehaviourAndStart(newTask: Runnable): Unit
}

/** Oggetto contenente implementazione di CustoScheduler e alcuni factory method */
object ClusterScheduler{

  /**implementa uno scheduler utilizzabile dagli attori del cluster specificato
   * espone funzioni di utility che facilitano il rimpiazzo del task schedulato
   *
   * @param initialDelay -> delay tra invocazione start e prima esecuzione del task
   * @param timeUnitInitialDelay -> unità di misura temporale per initialDelay
   * @param interval -> delay tra una invocazione del task schedulato e invocazione successiva
   * @param timeUnitInterval -> unità di misura temporale per interval
   * @param runnable -> Option contenente task che verrà eseguito
   * @param cluster -> cluster cui appartiene l'attore che usa lo scheduler
   */
  private class ClusterScheduler(var initialDelay: Long,
                         var timeUnitInitialDelay: TimeUnit,
                         var interval: Long,
                         var timeUnitInterval: TimeUnit,
                         var runnable: Option[Runnable],
                         cluster: Cluster) extends CustomScheduler{
    val ec = cluster.system.dispatcher
    def initialDelayTime = Duration.create(initialDelay, timeUnitInitialDelay)
    def intervalTime = Duration.create(interval, timeUnitInterval)

    //questo Optional contiene il Cancellable task in esecuzione
    var cancellable :Option[Cancellable] = None
    //true quando il task sta eseguendo
    var running:Boolean = false

    /** avvia lo scheduling dell'attività presente in cancellable */
    def startTask():Unit = {
      runnable match {
        case Some(action) => {
          cancellable = Some(cluster.system.scheduler.schedule(initialDelayTime, intervalTime, action)(ec))
          running = true
        }
        case _ => println("runnable non riconosciuto")
      }
    }

    /** permette di stoppare lo scheduler */
    def stopTask(): Unit = {
      if(running){
        running = false
        stopScheduler
      }
    }

    //stoppa l'esecuzione del task attualmente in esecuzione
    private def stopScheduler:Unit = {
      cancellable match {
        case Some(task) => task.cancel()
        case _ => println("Errore stop scheduler")
      }
    }

    //modifichi solo il runnable
    private def quickBehaviourReset(newTask: Runnable):Boolean = {
      if(running){
        false
      }else{
        runnable = Some(newTask)
        cancellable  = None
        true
      }
    }

    /** stoppa task corrente, lo rimpiazza con quello indicato e riavvia lo scheduler */
    def replaceBehaviourAndStart(newTask: Runnable): Unit ={
      stopTask() //aggiunto per sicurezza, se già stoppato non fa niente e passa alla prossima istruzione
      quickBehaviourReset(newTask)
      startTask()
    }
  }

  /** delay tra invocazione start e prima esecuzione del task */
  val INITIAL_DELAY :Long = 1;
  /** delay tra una invocazione del task schedulato e invocazione successiva */
  val INTERVAL :Long = 6;

  /** factory con apply per ClusterScheduler */
  def apply(cluster: Cluster): CustomScheduler =
    new ClusterScheduler(INITIAL_DELAY, TimeUnit.SECONDS, INTERVAL, TimeUnit.SECONDS, None,cluster)

  /**factory con apply per il ClusterScheduler in cui è possibile specificare tutti i parametri
   *
   * @param initialDelay -> delay tra invocazione start e prima esecuzione del task
   * @param timeUnitInitialDelay -> unità di misura temporale per initialDelay
   * @param interval -> delay tra una invocazione del task schedulato e invocazione successiva
   * @param timeUnitInterval -> unità di misura temporale per interval
   * @param runnable -> Option contenente task che verrà eseguito
   * @param cluster -> cluster cui appartiene l'attore che usa lo scheduler
   */
  def apply(initialDelay: Long,
            timeUnitInitialDelay: TimeUnit,
            interval: Long,
            timeUnitInterval: TimeUnit,
            runnable: Option[Runnable],
            cluster: Cluster): CustomScheduler =
    new ClusterScheduler(initialDelay, timeUnitInitialDelay, interval, timeUnitInterval, runnable, cluster)

}