# Original tests fromt he analyzer script.  Moved here so we don't lose them

def test_file():
    f = File('/home/icat/a.cpp', None, 0)
    assert f.guess_problem_id() == 'a'
            
    f = File('/home/icat/b/sol.c', None, 0)
    assert f.guess_problem_id() == 'b'
            
    f = File('/home/icat/prob-F__.java', None, 0)
    assert f.guess_problem_id() == 'f'

    f = File('/home/icat/balloons.java', None, 0)
    ids = {'a': ['balloon', 'balloons']}
    assert f.guess_problem_id(problem_identifiers=ids) == 'a'

    f = File('/home/icat/losning_b.c', None, 0)
    assert f.guess_problem_id() == 'b'
            
    f = File('/home/icat/b_2.c', None, 0)
    assert f.guess_problem_id() == 'b'

    f = File('/home/icat/template.cc', None, 0)
    assert f.guess_problem_id() == None

def test_file_list():
    now = datetime.now()
    delta = timedelta(minutes=3)

    d = File('/home/icat/d.cc', now, 2327)
    d_old = File('/home/icat/d_old.cc', now-delta, 2327)
    e = File('/home/icat/e.java', now, 2327)
    e_new = File('/home/icat/e_new.java', now+delta, 2327)
    template = File('/home/icat/template.java', now+delta, 2327)
    
    file_list = FileList([d, d_old, e, e_new, template])
    sol_files = file_list.guess_solution_files()
    assert sol_files == {'e': [e_new, e], 'd': [d, d_old]}

def test():
    test_file()
    test_file_list()


if __name__ == '__main__':
    test()
