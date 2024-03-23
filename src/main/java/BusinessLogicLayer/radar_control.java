package BusinessLogicLayer;

import Entities.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class radar_control {

    // Variables
    private Consumer<String, String> consumer;
    private Producer<String, String> producer;
    private ScheduledExecutorService scheduler;
    private Helper helper;
    private Positions positions;
    private Config config;
    private Calculator calculator;
    int targetXPosition;
    int targetYPosition;
    int radarXPosition;
    int radarYPosition;
    int cameraXPosition;
    int cameraYPosition;
    // Variables

    public radar_control(){
        initParameters();
    }

    private void initParameters(){
        helper = new Helper();
        config = Config.getInstance();
        positions = Positions.getInstance();
        calculator = new Calculator();
        scheduler = Executors.newScheduledThreadPool(1);
        consumer = createConsumer();
        producer = createProducer();
    }

    private Producer<String, String> createProducer(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "radar-control-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private Consumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "radar-control-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("TargetPointPosition", "TowerPosition"));
        return consumer;
    }

    public void startProducer(){
        scheduler.scheduleAtFixedRate(this::publishTargetBearingPosition, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    public void stopProducer() {
        scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void resumeProducer() {
        scheduler.scheduleAtFixedRate(this::publishTargetBearingPosition, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    private void publishTargetBearingPosition() {
        double distance = calculator.getCalculateDistance(
                radarXPosition,
                helper.getItemYPosition(config.getJFrameHeight(),radarYPosition),
                targetXPosition,
                helper.getItemYPosition(config.getJFrameHeight(),targetYPosition)
        );
        double angle = calculator.getAngleDeg(
                radarXPosition,
                helper.getItemYPosition(config.getJFrameHeight(),radarYPosition),
                targetXPosition,
                helper.getItemYPosition(config.getJFrameHeight(),targetYPosition)
        );
        String targetMessage = helper.createStringByJson(
                "angle",String.valueOf(angle),
                "distance",String.valueOf(distance)
        );
        producer.send(new ProducerRecord<>("TargetBearingPosition", "TargetBearingPosition", targetMessage));
    }

    public void startListenTargetPointPositionAndTowerPosition(){
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(config.getSensorListenmHZ()));
        records.forEach(record -> {

            JSONObject jsonObject;
            if("TargetPointPosition".equals(record.key())){
                try {
                    jsonObject = helper.returnJSONObjectByString(record.value());
                    targetXPosition = Integer.parseInt(jsonObject.get("targetXPosition").toString());
                    targetYPosition = Integer.parseInt(jsonObject.get("targetYPosition").toString());
                    updateTargetPosition(targetXPosition,targetYPosition);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            else if("TowerPosition".equals(record.key())){
                try {
                    jsonObject = helper.returnJSONObjectByString(record.value());
                    radarXPosition = Integer.parseInt(jsonObject.get("radarXPosition").toString());
                    radarYPosition = Integer.parseInt(jsonObject.get("radarYPosition").toString());
                    cameraXPosition = Integer.parseInt(jsonObject.get("cameraXPosition").toString());
                    cameraYPosition = Integer.parseInt(jsonObject.get("cameraYPosition").toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void updateTargetPosition(int targetXPosition, int targetYPosition){
        // Simulating plane coordinate
        if (targetXPosition < config.getPanelWidth() && positions.getPlaneXPosition() <= 300 && targetYPosition > 0 && (positions.getPlaneXPosition() != - 100)) {
            positions.setPlaneXPosition(targetXPosition + config.getTargetXVelocity());
            positions.setPlaneYPosition(targetYPosition - config.getTargetYVelocity());
        } else if (targetXPosition >= config.getPanelWidth()) {
            // out of scan area
            positions.setPlaneXPosition(-100);
            positions.setPlaneYPosition(385);
        } else if(positions.getPlaneXPosition() > 300 && targetYPosition > 0 && (positions.getPlaneXPosition() != - 100)){
            positions.setPlaneXPosition(targetXPosition + config.getTargetXVelocity());
            positions.setPlaneYPosition(targetYPosition + config.getTargetYVelocity());
        }
    }

}
