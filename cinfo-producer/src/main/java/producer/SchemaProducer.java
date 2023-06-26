package producer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;


public class SchemaProducer {
	public static void main(String[] args) {
		// Configurar las propiedades del productor
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19093,localhost:29093,localhost:39093");
		props.put("enhanced.avro.schema.support", true);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		System.out.println(KafkaAvroSerializer.class.getName());

		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"); // URL del registro de esquemas

		KafkaProducer<String, Object> producer = new KafkaProducer<>(props);

		int schemaId = getSchemaIdFromSchemaRegistry("sales-in-2", props.getProperty("schema.registry.url"));

		Schema schema = getSchemaFromSchemaRegistry(schemaId, props.getProperty("schema.registry.url"));

		GenericRecord message = new GenericData.Record(schema);
		message.put("Id", "123");
		message.put("OriginId", "456");
		message.put("OriginName", "Origen");
		message.put("TransactionDate", System.currentTimeMillis());
		//message.put("TransactionType", TransactionType.SALE);
		message.put("TransactionType", "Sale");
		message.put("Store", 1);
		message.put("Currency", "USD");
		message.put("Item", "Product 123");
		message.put("Quantity", 5);
		message.put("GrossPrice", 10.99);
		message.put("NetPrice", 9.99);
		message.put("DiscountValue", 1.0);
		message.put("TaxRate", 0.10);
		message.put("POS", "POS-01");
		message.put("PromotionId", "PROMO-123");
		message.put("PromotionCard", "CARD-456");
		message.put("ReturnOriginId", "789");
		message.put("ReturnToWarehouse", 2);
		message.put("ClientIdentification", 123456789L);
		// Enviar el mensaje al tema de Kafka utilizando el ID del esquema
		ProducerRecord<String, Object> record = new ProducerRecord<>("sales_in", message);
		Future<RecordMetadata> result = producer.send(record);
		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Cerrar el productor de Kafka
		producer.close();
	}

	private static int getSchemaIdFromSchemaRegistry(String schemaName, String schemaRegistryUrl) {
		io.confluent.kafka.schemaregistry.client.SchemaRegistryClient schemaRegistryClient = new io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient(
				schemaRegistryUrl, 1000);

		try {
			return schemaRegistryClient.getLatestSchemaMetadata(schemaName).getId();
		} catch (io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException | IOException e) {
			e.printStackTrace();
			// Manejo de errores
		}

		return -1;
	}

	private static Schema getSchemaFromSchemaRegistry(int schemaId, String schemaRegistryUrl) {
		io.confluent.kafka.schemaregistry.client.SchemaRegistryClient schemaRegistryClient = new io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient(
				schemaRegistryUrl, 1000);

		try {
			return schemaRegistryClient.getByID(schemaId);
		} catch (io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException | IOException e) {
			e.printStackTrace();
			// Manejo de errores
		}

		return null;
	}
}
