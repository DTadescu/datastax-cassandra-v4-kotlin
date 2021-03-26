package shdv.demo.cas4.repository.cassandra.producers

import com.datastax.oss.driver.api.mapper.result.MapperResultProducer
import com.datastax.oss.driver.api.mapper.result.MapperResultProducerService
import java.util.*

class GuavaFutureProducerService: MapperResultProducerService {
    override fun getProducers(): MutableIterable<MapperResultProducer> =
        Arrays.asList(FutureOfEntityProducer())
}
