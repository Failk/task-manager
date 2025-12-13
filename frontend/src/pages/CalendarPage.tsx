import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar } from '../components/calendar';

const CalendarPage: React.FC = () => {
  const navigate = useNavigate();

  const handleTaskClick = (id: number) => {
    navigate(`/tasks/${id}`);
  };

  return (
    <div>
      <Calendar onTaskClick={handleTaskClick} />
    </div>
  );
};

export default CalendarPage;
