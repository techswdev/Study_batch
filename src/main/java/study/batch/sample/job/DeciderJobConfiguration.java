package study.batch.sample.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DeciderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     *   startStep -> oddDecider에서 홀수 인지 짝수인지 구분 -> oddStep or evenStep 진행
     */

    @Bean
    public Job deciderJob() {
        return jobBuilderFactory.get("deciderJob")
                .start(startStep())
                  .next(decider()) // 홀수 짝수 구분
                .from(decider())//decider 상태가
                    .on("ODD") // 홀수라면
                    .to(oddStep())//oddstep( ) 실행
                .from(decider()) //decider의 상태가
                    .on("EVEN") // 짝수라면
                    .to(evenStep()) //evenstep ( ) 실행
                .end()
                .build();
    }

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("startStep")
                .tasklet((contribution, chunkContext) ->
                {
                    log.info(">>>>> Start!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step evenStep() {
        return stepBuilderFactory.get("evenStep")
                .tasklet((contribution, chunkContext) ->
                {
                    log.info(">>>>> 짝수입니다.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step oddStep() {
        return stepBuilderFactory.get("oddStep")
                .tasklet((contribution, chunkContext) ->
                {
                    log.info(">>>>> 홀수입니다.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public JobExecutionDecider decider(){
        return new OddDecider();
    }

    public  static class OddDecider implements JobExecutionDecider{
        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

            Random random = new Random();

            int radonNumber = random.nextInt(50)+1;
            log.info("랜덤숫자: {}", radonNumber);

            if(radonNumber % 2==0){

                return new FlowExecutionStatus("EVEN");
            } else {

                return new FlowExecutionStatus("ODD");
            }
        }
    }
}
