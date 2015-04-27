#encoding:utf8

from gensim import *
import codecs

def get_eev(l,model):
	l=l.strip("()").lower()
 	l=l.split(', ') 
 #   e1 = n1.lstrip('(')
 #   e2 = n2.rstrip(')')
	try:
		v = model.similarity(l[0],l[1])
 		return "("+l[0]+", "+l[1]+")="+str(v)
 	except Exception, e:
		return "("+l[0]+", "+l[1]+")=-1"
 	

print 'start load model\n'
model = models.Word2Vec.load_word2vec_format('../library/GoogleNews-vectors-negative300.bin', binary=True)
print 'finish load'

# read
print 'read attribute list'
attrFiles = codecs.open('../data/attrs.txt')
attrs = attrFiles.readlines()
attrs=[a.strip("\n") for a in attrs]
print 'start calculate'
attr='founder'
#for attr in attrs:
print 'calculate ' + attr
fin = codecs.open('../data/attr-raw/'+attr+'.txt')
arr = fin.readlines()
arr=[a.strip("\n") for a in arr]
print len(arr)
fin.close()

li_eev = [get_eev(l,model)  for l in arr]
fo =  codecs.open('../data/attr-sim/'+attr+'.txt','w')
for eev in li_eev:
	ss = eev+'\n'
	fo.write(ss)
fo.close()