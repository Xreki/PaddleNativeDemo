#include "timer.h"
#include <stdio.h>
#include <sys/time.h>

namespace utils {

double TimingUnit::get_current_time() {
  struct timeval tp;
  gettimeofday(&tp, NULL);
  return ((double) tp.tv_sec + (double) tp.tv_usec * 1.0E-6);
}

void Timer::add(std::string name, std::string parent) {
  if (name == "") {
    return;
  }

  if (_timer.find(name) == _timer.end()) {
    int level = 0;
    int id = 0;
    if (parent != "" && _timer.find(parent) != _timer.end()) {
      level = _timer[parent]._level + 1;
      id = _timer[parent]._number_of_children;

      _timer[parent]._number_of_children += 1;
    }

    if (level >= _number_of_levels) {
      _number_of_levels = level + 1;
    }

    _timer[name] = TimerUnit(level, id, parent);
  }
}

std::string Timer::trans_to_string() {
  //return trans_to_string(0, "");
  return trans_to_string_in_order(0, "");
}

void Timer::print() {
  print_in_order(0, "");
  clear();
}

std::string Timer::trans_to_string(int level, std::string parent) {
  std::stringstream result;

  for (std::map<std::string, TimerUnit>::iterator iter = _timer.begin();
      iter != _timer.end(); ++iter) {
    if (iter->second._level == level &&
        iter->second._parent.compare(parent) == 0) {
      for (int i = 0; i < level; ++i) {
        result << "....";
      }
      result << iter->first << ": "
             << iter->second._seconds * 1000 << " ms, "
             << iter->second._seconds * 100 / _seconds << "%\n";
      result << trans_to_string(level + 1, iter->first);
    }
  }

  return result.str();
}

std::string Timer::trans_to_string_in_order(int level, std::string parent) {
  std::stringstream result;

  int number_of_children = 0;
  if (level == 0) {
    number_of_children = 1;
  } else if (_timer.find(parent) != _timer.end()) {
    number_of_children = _timer[parent]._number_of_children;
  } else {
    number_of_children = 0;
  }

  for (int id = 0; id < number_of_children; ++id) {
    for (std::map<std::string, TimerUnit>::iterator iter = _timer.begin();
        iter != _timer.end(); ++iter) {
      if (iter->second._level == level && iter->second._id == id
          && iter->second._parent.compare(parent) == 0) {
        for (int i = 0; i < level; ++i) {
          result << "....";
        }
        result << "[" << iter->second._id << "] "
               << iter->first << ": " << iter->second._seconds * 1000 << " ms"
               //<< ", " << iter->second._seconds * 100 / _seconds << "%"
               << "\n";
        result << trans_to_string_in_order(level + 1, iter->first);
      }
    }
  }

  return result.str();
}

void Timer::print_in_order(int level, std::string parent) {
  int number_of_children = 0;
  if (level == 0) {
    number_of_children = 1;
  } else if (_timer.find(parent) != _timer.end()) {
    number_of_children = _timer[parent]._number_of_children;
  } else {
    number_of_children = 0;
  }

  for (int id = 0; id < number_of_children; ++id) {
    for (std::map<std::string, TimerUnit>::iterator iter = _timer.begin();
        iter != _timer.end(); ++iter) {
      if (iter->second._level == level && iter->second._id == id &&
          iter->second._parent.compare(parent) == 0) {
        std::stringstream result;
        for (int i = 0; i < level; ++i) {
          result << "....";
        }
        result << "[" << iter->second._id << "] "
               << iter->first << ": " << iter->second._seconds * 1000 << " ms"
               //<< ", " << iter->second._seconds * 100 / _seconds << "%"
               << "\n";
        fprintf(stdout, "%s", result.str().c_str());
        print_in_order(level + 1, iter->first);
      }
    }
  }
}

Timer g_utils_timer;

} // namespace utils
