curl -XDELETE "http://192.168.0.23:9200/moqatest"
curl -XPUT "http://192.168.0.23:9200/moqatest"
curl -XPOST "http://192.168.0.23:9200/_bulk?pretty" --data-binary @bulk_questions.txt
curl -XPOST "http://192.168.0.23:9200/_bulk?pretty" --data-binary @bulk_answers.txt
curl -XPOST "http://192.168.0.23:9200/_bulk?pretty" --data-binary @bulk_comments.txt
