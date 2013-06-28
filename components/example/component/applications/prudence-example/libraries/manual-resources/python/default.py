
# Note: Python will not let us use the "import" keyword with a directory that contains a dash

person = __import__('manual-resources/python/person')

resources = {
    'person': person.Person()}
