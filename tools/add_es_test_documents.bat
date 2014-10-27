curl -XDELETE "http://68.149.53.165:9182/moqatest"
curl -XPUT "http://68.149.53.165:9182/moqatest"
curl -XPOST "http://68.149.53.165:9182/_bulk?pretty" --data-binary @bulk_questions.txt
curl -XPOST "http://68.149.53.165:9182/_bulk?pretty" --data-binary @bulk_answers.txt
curl -XPOST "http://68.149.53.165:9182/_bulk?pretty" --data-binary @bulk_comments.txt
