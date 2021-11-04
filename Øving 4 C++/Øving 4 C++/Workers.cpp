#include <iostream>
#include <thread>
#include <condition_variable>
#include <functional>
#include <list>

using namespace std;
using namespace chrono;

class Workers {

    int threadNr;
    list<thread> threads;
    list<function<void()>> tasks;
    mutex tasks_mutex;
    mutex stopped_mutex;
    mutex wait_mutex;
    bool stopped = false;
    bool wait = true;
    condition_variable condition;


public:
    Workers(int nr_threads) {
        threadNr = nr_threads;
    }


    void start() {
        threads.clear();
        for (int i = 0; i < threadNr; i++) {
            threads.emplace_back([this, i] {
                bool done = false;
                bool remaining_tasks = false;
                int nr = i;
                printf("Thread: %d\n", nr + 1);
                while (!done) {
                    function<void()> task;
                    if (!remaining_tasks) {
                        unique_lock<mutex> lock(wait_mutex);
                        while (wait) {
                            condition.wait(lock);
                        }
                    }
                    work(task, remaining_tasks, done);
                    if (task) {
                        task();
                        condition.notify_one();
                    }
                }
                });
        }
    }

    void work(function<void()>& task, bool& remaining_tasks, bool& done) {
        {
            lock_guard<mutex> lock(tasks_mutex);
            if (!tasks.empty()) {
                task = *tasks.begin();
                tasks.pop_front();
                remaining_tasks = true;

            }
            else {
                {
                    unique_lock<mutex> lock(stopped_mutex);
                    if (stopped) {
                        done = true;
                    }
                    else {
                        unique_lock<mutex> lock(wait_mutex);
                        wait = true;
                        remaining_tasks = false;
                    }
                }
            }
        }
    }

    void post(function<void()> task) {
        {
            lock_guard<mutex> lock(tasks_mutex);
            tasks.push_back(task);
        }

        {
            lock_guard<mutex> lock(wait_mutex);
            wait = false;
        }
        condition.notify_one();
    }

    void post_sleep(function<void()> task) {
        {
            lock_guard<mutex> lock(tasks_mutex);
            tasks.push_back([task] {
                this_thread::sleep_for(chrono::milliseconds(2000));
                task();
                });
        }

        {
            lock_guard<mutex> lock(wait_mutex);
            wait = false;
        }

        condition.notify_one();
    }

    void stop() {

        unique_lock<mutex> lock(stopped_mutex);
        stopped = true;

        {
            lock_guard<mutex> lock(wait_mutex);
            wait = false;
        }

        condition.notify_all();

        for (thread& thread : threads) {
            thread.join();
        }
    }

};

int main() {
    Workers workers(4);
    Workers event_loop(1);
    workers.start();
    event_loop.start();

    workers.post_sleep([] {
        printf("sleeping task a \n");
        });

    workers.post([] {
        printf("task b \n");
        });
    event_loop.post([] {
        printf("task c \n");
        });
    event_loop.post([] {
        printf("task d \n");
        });

    workers.stop();
    event_loop.stop();

    return 0;
}